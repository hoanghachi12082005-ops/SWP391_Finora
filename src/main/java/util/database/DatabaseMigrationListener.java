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
            
            // Check if failed_login_count column exists in Employee table
            boolean failedLoginExists = false;
            try (ResultSet rs = metaData.getColumns(null, null, "Employee", "failed_login_count")) {
                if (rs.next()) failedLoginExists = true;
            }
            if (!failedLoginExists) {
                try (ResultSet rs = metaData.getColumns(null, null, "employee", "failed_login_count")) {
                    if (rs.next()) failedLoginExists = true;
                }
            }
            if (!failedLoginExists) {
                try (ResultSet rs = metaData.getColumns(null, null, "Employee", "FailedLoginCount")) {
                    if (rs.next()) failedLoginExists = true;
                }
            }
            if (!failedLoginExists) {
                try (ResultSet rs = metaData.getColumns(null, null, "employee", "FailedLoginCount")) {
                    if (rs.next()) failedLoginExists = true;
                }
            }

            if (!failedLoginExists) {
                System.out.println("Column 'failed_login_count' does not exist. Altering Employee table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Employee ADD failed_login_count INT NOT NULL DEFAULT 0");
                    System.out.println("Column 'failed_login_count' added successfully to Employee table.");
                } catch (SQLException ex) {
                    System.err.println("Failed to alter Employee table: " + ex.getMessage());
                }
            }

            // Auto-heal dummy hashes and reset failed login attempts for local development
            try {
                String sqlUpdateHashes = "UPDATE Employee SET passwordHash = ? WHERE passwordHash = '$2a$10$dummyhashfordemo'";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateHashes)) {
                    ps.setString(1, "$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q");
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        System.out.println("Updated " + updated + " employees with dummy hashes to valid '123456' hashes.");
                    }
                }
                String sqlResetLogins = "UPDATE Employee SET failed_login_count = 0, status = 'ACTIVE' WHERE failed_login_count >= 4 OR status = 'INACTIVE'";
                try (Statement stmt = conn.createStatement()) {
                    int resetCount = stmt.executeUpdate(sqlResetLogins);
                    if (resetCount > 0) {
                        System.out.println("Reset failed login counts and unlocked " + resetCount + " accounts.");
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Failed to auto-heal employee password hashes/statuses: " + ex.getMessage());
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
                        CONSTRAINT FK_Shift_Branch FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
                    );
                    """;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlCreateShift);
                    System.out.println("Table 'shift' created successfully.");
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
