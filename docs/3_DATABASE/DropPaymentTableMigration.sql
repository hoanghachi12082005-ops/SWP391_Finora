USE [DBFinoraV3];
GO

-- Drop table payment if it exists
IF OBJECT_ID('dbo.payment', 'U') IS NOT NULL
BEGIN
    DROP TABLE [dbo].[payment];
    PRINT 'Dropped table payment successfully.';
END
ELSE
BEGIN
    PRINT 'Table payment does not exist or was already dropped.';
END
GO

-- Add description column to [order] table if it doesn't exist
IF COL_LENGTH('[dbo].[order]', 'description') IS NULL
BEGIN
    ALTER TABLE [dbo].[order] ADD [description] NVARCHAR(500) NULL;
    PRINT 'Added description column to order table.';
END
ELSE
BEGIN
    PRINT 'description column already exists in order table.';
END
GO
