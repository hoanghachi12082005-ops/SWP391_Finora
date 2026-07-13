-- =============================================
-- Script: Tự động hủy đơn hàng PENDING quá 30 phút
-- Phương thức: SQL Server Agent Job (chạy mỗi 1 phút)
-- =============================================
USE [DBFinoraV3];
GO

-- Kiểm tra xem job đã tồn tại chưa
IF EXISTS (SELECT job_id FROM msdb.dbo.sysjobs WHERE name = N'AutoCancelPendingOrders')
BEGIN
    PRINT N'Job AutoCancelPendingOrders đã tồn tại.';
    RETURN;
END
GO

-- Bước 1: Tạo stored procedure xử lý cancel
CREATE OR ALTER PROCEDURE [dbo].[sp_AutoCancelPendingOrders]
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @cutoffTime DATETIME = DATEADD(MINUTE, -30, GETDATE());

    UPDATE [order]
    SET status = 'CANCELLED'
    WHERE status = 'PENDING'
      AND created_at <= @cutoffTime;

    PRINT N'Đã hủy các đơn hàng PENDING quá 30 phút (trước ' + CONVERT(VARCHAR, @cutoffTime, 120) + N').';
END
GO

-- Bước 2: Tạo SQL Server Agent Job
DECLARE @jobId BINARY(16);

EXEC msdb.dbo.sp_add_job
    @job_name = N'AutoCancelPendingOrders',
    @enabled = 1,
    @description = N'Tự động hủy các đơn hàng ở trạng thái PENDING quá 30 phút',
    @owner_login_name = N'sa',
    @job_id = @jobId OUTPUT;

-- Bước 3: Tạo job step (gọi stored procedure)
EXEC msdb.dbo.sp_add_jobstep
    @job_id = @jobId,
    @step_name = N'CancelPendingOrders',
    @command = N'EXEC [dbo].[sp_AutoCancelPendingOrders]',
    @database_name = N'DBFinoraV3',
    @on_success_action = 1;  -- 1 = Quit with success

-- Bước 4: Tạo schedule (chạy mỗi 5 phút)
DECLARE @scheduleId INT;
EXEC msdb.dbo.sp_add_schedule
    @schedule_name = N'Every5Minutes',
    @freq_type = 4,           -- 4 = Daily
    @freq_interval = 1,       -- Every 1 day
    @freq_subday_type = 4,    -- 4 = Minutes
    @freq_subday_interval = 5,-- Every 5 minutes
    @active_start_time = 0,   -- 00:00:00
    @active_end_time = 235959,-- 23:59:59
    @schedule_id = @scheduleId OUTPUT;

-- Gán schedule vào job
EXEC msdb.dbo.sp_attach_schedule
    @job_id = @jobId,
    @schedule_id = @scheduleId;

-- Bước 5: Thêm job vào server
EXEC msdb.dbo.sp_add_jobserver
    @job_id = @jobId,
    @server_name = N'(LOCAL)';

PRINT N'Đã tạo job AutoCancelPendingOrders thành công.';
GO
