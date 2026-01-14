-- ============================================================
-- University Admission System - Stored Procedures
-- ============================================================

-- ============================================================
-- ADMIN PROCEDURES
-- ============================================================

-- Procedure: Check if admin email already exists
-- Input: @Email (email to check)
-- Output: @Exists (1 if exists, 0 if not)
CREATE PROCEDURE sp_CheckAdminEmailExists
    @Email NVARCHAR(100),
    @Exists BIT OUTPUT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.Admin WHERE Email = @Email)
        SET @Exists = 1
    ELSE
        SET @Exists = 0
END
GO

-- Procedure: Insert new admin record
-- Input: @Email, @Password, @IsSuperAdmin
CREATE PROCEDURE sp_InsertNewAdmin
    @Email NVARCHAR(100),
    @Password NVARCHAR(100),
    @IsSuperAdmin BIT
AS
BEGIN
    INSERT INTO dbo.Admin (Email, Password, IsSuperAdmin)
    VALUES (@Email, @Password, @IsSuperAdmin)
END
GO

-- ============================================================
-- APPLICANT PROCEDURES
-- ============================================================

-- Procedure: Update application status with admin ID
-- Purpose: Approve or reject application and record which admin made the decision
-- Used by: ApplicantManager.updateApplicationStatusWithAdmin()
-- Input: @ApplicationId, @Status, @AdminId
CREATE PROCEDURE sp_UpdateApplicationStatus
    @ApplicationId INT,
    @Status NVARCHAR(50),
    @AdminId INT
AS
BEGIN
    UPDATE dbo.ApplicationForm 
    SET status = @Status, AdminID = @AdminId 
    WHERE application_form_id = @ApplicationId
END
GO

-- Procedure: Get application status by ID
-- Purpose: Check current status of an application before processing
-- Used by: ApplicantManager.getApplicationStatus()
-- Input: @ApplicationId
-- Output: @Status
CREATE PROCEDURE sp_GetApplicationStatus
    @ApplicationId INT,
    @Status NVARCHAR(50) OUTPUT
AS
BEGIN
    SELECT @Status = status 
    FROM dbo.ApplicationForm 
    WHERE application_form_id = @ApplicationId
END
GO

-- Procedure: Insert new application form
-- Purpose: Save new application and return auto-generated application ID
-- Used by: ApplicantManager.saveApplication()
CREATE PROCEDURE sp_InsertApplication
    @ApplicantID INT = NULL,
    @AdminID INT = NULL,
    @Email NVARCHAR(100),
    @FirstName NVARCHAR(50),
    @LastName NVARCHAR(50),
    @DateOfBirth DATE = NULL,
    @Gender NVARCHAR(10) = NULL,
    @TwelfthPercentage DECIMAL(5,2) = NULL,
    @TwelfthYear INT = NULL,
    @TwelfthStream NVARCHAR(50) = NULL,
    @UniversityName NVARCHAR(100) = NULL,
    @TestSchedule DATETIME = NULL,
    @TestScore INT = NULL,
    @Status NVARCHAR(50),
    @FeeStatus NVARCHAR(50),
    @IsSubmitted BIT,
    @IsScholarshipSubmitted BIT,
    @ProgramID INT = NULL,
    @ApplicationFormID INT OUTPUT
AS
BEGIN
    INSERT INTO dbo.ApplicationForm (
        ApplicantID, AdminID, email, first_name, last_name, date_of_birth, gender,
        twelfth_percentage, twelfth_year, twelfth_stream,
        university_name, test_schedule, test_score, status, fee_status,
        is_submitted, is_scholarship_submitted, programid
    ) VALUES (
        @ApplicantID, @AdminID, @Email, @FirstName, @LastName, @DateOfBirth, @Gender,
        @TwelfthPercentage, @TwelfthYear, @TwelfthStream,
        @UniversityName, @TestSchedule, @TestScore, @Status, @FeeStatus,
        @IsSubmitted, @IsScholarshipSubmitted, @ProgramID
    )
    
    SET @ApplicationFormID = SCOPE_IDENTITY()
END
GO

-- Procedure: Check if applicant already applied for program
-- Purpose: Prevent duplicate applications for same program
-- Used by: ApplicantManager.hasAppliedBefore()
CREATE PROCEDURE sp_CheckDuplicateApplication
    @ApplicantID INT,
    @ProgramID INT,
    @Exists BIT OUTPUT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.ApplicationForm WHERE ApplicantID = @ApplicantID AND programid = @ProgramID)
        SET @Exists = 1
    ELSE
        SET @Exists = 0
END
GO

-- Procedure: Update application status (without admin ID)
-- Purpose: Update status for applications
-- Used by: ApplicantManager.updateApplicationStatus()
CREATE PROCEDURE sp_UpdateApplicationStatusOnly
    @ApplicationId INT,
    @Status NVARCHAR(50)
AS
BEGIN
    UPDATE dbo.ApplicationForm 
    SET status = @Status 
    WHERE application_form_id = @ApplicationId
END
GO

-- Procedure: Update test schedule for application
-- Purpose: Set test date and time for applicant
-- Used by: ApplicantManager.updateTestSchedule()
CREATE PROCEDURE sp_UpdateTestSchedule
    @ApplicationId INT,
    @TestSchedule DATETIME
AS
BEGIN
    UPDATE dbo.ApplicationForm 
    SET test_schedule = @TestSchedule 
    WHERE application_form_id = @ApplicationId
END
GO

-- ============================================================
-- COLLEGE PROCEDURES
-- ============================================================

-- Procedure: Insert new college
-- Purpose: Add a new college and return the auto-generated college ID
-- Used by: CollegeManager.addCollege()
-- Input: @CollegeName
-- Output: @CollegeId (auto-generated ID)
CREATE PROCEDURE sp_InsertCollege
    @CollegeName NVARCHAR(100),
    @CollegeId INT OUTPUT
AS
BEGIN
    INSERT INTO dbo.College (college_name)
    VALUES (@CollegeName)
    
    SET @CollegeId = SCOPE_IDENTITY()
END
GO

-- Procedure: Delete college by name
-- Purpose: Remove a college from the database by name
-- Used by: CollegeManager.removeCollegeByName()
-- Input: @CollegeName
-- Output: @RowsAffected (number of rows deleted)
CREATE PROCEDURE sp_DeleteCollegeByName
    @CollegeName NVARCHAR(100),
    @RowsAffected INT OUTPUT
AS
BEGIN
    DELETE FROM dbo.College 
    WHERE college_name = @CollegeName
    
    SET @RowsAffected = @@ROWCOUNT
END
GO

-- ============================================================
-- PROGRAM PROCEDURES
-- ============================================================

-- Procedure: Add new program with stream associations
-- Purpose: Insert program and associate multiple streams in single transaction
-- Used by: ProgramManager.addProgram()
CREATE PROCEDURE sp_AddProgram
    @ProgramName NVARCHAR(100),
    @CollegeID INT,
    @Seats INT,
    @Eligibility INT,
    @Fee DECIMAL(10,2),
    @StreamIDs NVARCHAR(MAX), -- Comma-separated stream IDs
    @ProgramID INT OUTPUT
AS
BEGIN
    BEGIN TRANSACTION
    BEGIN TRY
        -- Insert program record
        INSERT INTO dbo.Program (ProgramName, College_ID, Seats, Eligibility, Fee)
        VALUES (@ProgramName, @CollegeID, @Seats, @Eligibility, @Fee)
        
        SET @ProgramID = SCOPE_IDENTITY()
        
        -- Insert stream associations if provided
        IF @StreamIDs IS NOT NULL AND LEN(@StreamIDs) > 0
        BEGIN
            INSERT INTO dbo.ProgramStream (programid, stream_id)
            SELECT @ProgramID, CAST(LTRIM(RTRIM(value)) AS INT)
            FROM STRING_SPLIT(@StreamIDs, ',')
            WHERE LTRIM(RTRIM(value)) <> ''
        END
        
        COMMIT TRANSACTION
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION
        THROW
    END CATCH
END
GO

-- Procedure: Remove program by ID
-- Purpose: Delete program record (cascade deletes program-stream associations)
-- Used by: ProgramManager.removeProgram()
CREATE PROCEDURE sp_RemoveProgram
    @ProgramID INT,
    @RowsAffected INT OUTPUT
AS
BEGIN
    DELETE FROM dbo.Program WHERE ProgramID = @ProgramID
    SET @RowsAffected = @@ROWCOUNT
END
GO

-- ============================================================
-- ENTRY TEST PROCEDURES
-- ============================================================

-- Procedure: Save or update entry test record with subjects
-- Purpose: Insert or update test record and manage associated subjects
-- Used by: EntryTestRecordManager.saveRecord()
CREATE PROCEDURE sp_SaveEntryTestRecord
    @ApplicationFormID INT,
    @TestDateTime DATETIME,
    @Passed BIT,
    @Score INT,
    @Subjects NVARCHAR(MAX) -- Comma-separated subject names
AS
BEGIN
    -- Check if record exists
    IF EXISTS (SELECT 1 FROM dbo.EntryTestRecord WHERE Application_Form_ID = @ApplicationFormID)
    BEGIN
        -- Update existing record
        UPDATE dbo.EntryTestRecord 
        SET TestDateTime = @TestDateTime, Passed = @Passed, Score = @Score
        WHERE Application_Form_ID = @ApplicationFormID
    END
    ELSE
    BEGIN
        -- Insert new record
        INSERT INTO dbo.EntryTestRecord (Application_Form_ID, TestDateTime, Passed, Score)
        VALUES (@ApplicationFormID, @TestDateTime, @Passed, @Score)
    END
    
    -- Delete old subjects
    DELETE FROM dbo.EntryTestSubjects WHERE Application_Form_ID = @ApplicationFormID
    
    -- Insert new subjects if provided
    IF @Subjects IS NOT NULL AND LEN(@Subjects) > 0
    BEGIN
        -- Parse comma-separated subjects and insert them
        INSERT INTO dbo.EntryTestSubjects (Application_Form_ID, SubjectName)
        SELECT @ApplicationFormID, LTRIM(RTRIM(value))
        FROM STRING_SPLIT(@Subjects, ',')
        WHERE LTRIM(RTRIM(value)) <> ''
    END
END
GO

-- ============================================================
-- END OF STORED PROCEDURES
-- ============================================================
