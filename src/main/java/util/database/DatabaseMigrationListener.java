package util.database;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebListener
public class DatabaseMigrationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Starting Finora Database Migration...");
        try (Connection conn = DBContext.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Check if count_login_fail column exists in Employee table
            boolean countLoginFailExists = false;
            try (ResultSet rs = metaData.getColumns(null, null, "Employee", "count_login_fail")) {
                if (rs.next()) { countLoginFailExists = true; }
            }
            if (!countLoginFailExists) {
                try (ResultSet rs = metaData.getColumns(null, null, "employee", "count_login_fail")) {
                    if (rs.next()) { countLoginFailExists = true; }
                }
            }

            if (!countLoginFailExists) {
                System.out.println("Column 'count_login_fail' does not exist. Adding column to Employee table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Employee ADD count_login_fail INT DEFAULT 0 NOT NULL");
                    System.out.println("Column 'count_login_fail' added successfully to Employee table.");
                } catch (SQLException ex) {
                    System.err.println("Failed to add column 'count_login_fail' to Employee table: " + ex.getMessage());
                }
            }

            // Drop CHECK constraint on [order].status to allow IN_TRANSIT, COMPLETED, PENDING, CANCELLED, REJECTED
            try (Statement stmt = conn.createStatement()) {
                String sqlDropConstraint = """
                    DECLARE @sql NVARCHAR(MAX) = '';
                    SELECT @sql += 'ALTER TABLE [dbo].[order] DROP CONSTRAINT [' + cc.name + ']; '
                    FROM sys.check_constraints cc
                    JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
                    WHERE cc.parent_object_id = OBJECT_ID('dbo.order') AND c.name = 'status';
                    IF @sql <> '' EXEC sp_executesql @sql;
                """;
                stmt.execute(sqlDropConstraint);
                System.out.println("Check constraints on [order].status verified/dropped successfully.");
            } catch (SQLException ex) {
                System.err.println("Failed to drop check constraint on [order].status: " + ex.getMessage());
            }

            // Add actual_quantity column to order_detail & stock_transfer_detail if not exists
            try (Statement stmt = conn.createStatement()) {
                String sqlAddActualQty = """
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND name = 'actual_quantity')
                    BEGIN
                        ALTER TABLE [dbo].[order_detail] ADD [actual_quantity] INT NULL;
                    END
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND name = 'supplier_status')
                    BEGIN
                        ALTER TABLE [dbo].[order_detail] ADD [supplier_status] NVARCHAR(20) NULL;
                    END
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND name = 'supplier_id')
                    BEGIN
                        ALTER TABLE [dbo].[order_detail] ADD [supplier_id] INT NULL;
                    END
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[stock_transfer_detail]') AND name = 'actual_quantity')
                    BEGIN
                        ALTER TABLE [dbo].[stock_transfer_detail] ADD [actual_quantity] INT NULL;
                    END
                """;
                stmt.execute(sqlAddActualQty);
            } catch (SQLException ex) {
                System.err.println("Failed to add columns to order_detail: " + ex.getMessage());
            }

            boolean shiftExists = false;
            boolean cashTxExists = false;
            boolean auditLogExists = false;

            try (ResultSet rs = metaData.getTables(null, null, "shift", new String[]{"TABLE"})) {
                if (rs.next()) shiftExists = true;
            }
            if (!shiftExists) {
                try (ResultSet rs = metaData.getTables(null, null, "SHIFT", new String[]{"TABLE"})) {
                    if (rs.next()) shiftExists = true;
                }
            }

            try (ResultSet rs = metaData.getTables(null, null, "cash_transaction", new String[]{"TABLE"})) {
                if (rs.next()) cashTxExists = true;
            }
            if (!cashTxExists) {
                try (ResultSet rs = metaData.getTables(null, null, "CASH_TRANSACTION", new String[]{"TABLE"})) {
                    if (rs.next()) cashTxExists = true;
                }
            }

            try (ResultSet rs = metaData.getTables(null, null, "audit_log", new String[]{"TABLE"})) {
                if (rs.next()) auditLogExists = true;
            }
            if (!auditLogExists) {
                try (ResultSet rs = metaData.getTables(null, null, "AUDIT_LOG", new String[]{"TABLE"})) {
                    if (rs.next()) auditLogExists = true;
                }
            }

            if (!shiftExists) {
                System.out.println("Table 'shift' does not exist. Creating table...");
                String sqlCreateShift = """
                    CREATE TABLE shift (
                        shift_id        INT           IDENTITY(1,1) PRIMARY KEY,
                        emp_id          INT           NOT NULL,
                        branch_id       INT           NOT NULL,
                        opening_cash    DECIMAL(18,2) DEFAULT 0,
                        closing_cash    DECIMAL(18,2) NULL,
                        expected_cash   DECIMAL(18,2) NULL,
                        status          NVARCHAR(20)  DEFAULT 'OPEN' CHECK (status IN ('OPEN','CLOSED')),
                        opened_at       DATETIME      DEFAULT GETDATE(),
                        closed_at       DATETIME      NULL,
                        CONSTRAINT FK_Shift_Employee FOREIGN KEY (emp_id) REFERENCES Employee(emp_id),
                        CONSTRAINT FK_Shift_Branch FOREIGN KEY (branch_id) REFERENCES Branch(branch_id),
                        closing_note    NVARCHAR(500) NULL
                    );
                    """;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlCreateShift);
                    System.out.println("Table 'shift' created successfully.");
                }
            } else {
                // Add closing_note column if not exists (for existing databases)
                try (ResultSet cols = metaData.getColumns(null, null, "shift", "closing_note")) {
                    if (!cols.next()) {
                        String sqlAlter = "ALTER TABLE shift ADD closing_note NVARCHAR(500) NULL";
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sqlAlter);
                            System.out.println("Column 'closing_note' added to shift table.");
                        }
                    }
                }
            }

            if (!cashTxExists) {
                System.out.println("Table 'cash_transaction' does not exist. Creating table...");
                String sqlCreateCashTx = """
                    CREATE TABLE cash_transaction (
                        cash_transaction_id INT           IDENTITY(1,1) PRIMARY KEY,
                        shift_id             INT           NOT NULL,
                        type                 NVARCHAR(20)  CHECK (type IN ('WITHDRAW','DEPOSIT')),
                        amount               DECIMAL(18,2) NOT NULL,
                        note                 NVARCHAR(255),
                        created_at           DATETIME      DEFAULT GETDATE(),
                        CONSTRAINT FK_CashTransaction_Shift FOREIGN KEY (shift_id) REFERENCES shift(shift_id)
                    );
                    """;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlCreateCashTx);
                    System.out.println("Table 'cash_transaction' created successfully.");
                }
            }

            if (!auditLogExists) {
                System.out.println("Table 'audit_log' does not exist. Creating table...");
                String sqlCreateAuditLog = """
                    CREATE TABLE audit_log (
                        audit_log_id INT IDENTITY(1,1) PRIMARY KEY,
                        emp_id INT NOT NULL,
                        action_name NVARCHAR(50) NOT NULL,
                        table_name NVARCHAR(50) NOT NULL,
                        record_id INT NOT NULL,
                        old_data NVARCHAR(MAX) NULL,
                        new_data NVARCHAR(MAX) NULL,
                        created_at DATETIME DEFAULT GETDATE(),
                        CONSTRAINT FK_AuditLog_Employee FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
                    );
                    """;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlCreateAuditLog);
                    System.out.println("Table 'audit_log' created successfully.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
