-- ============================================================
-- University Admission System - Views
-- ============================================================

-- ============================================================
-- APPLICATION VIEWS
-- ============================================================

-- View: All Applications with Program Information
-- Purpose: Display all application details with program names for admin viewing
-- Used by: ApplicantManager.loadAllApplications()
-- Joins ApplicationForm with Program table to get program names
CREATE VIEW vw_AllApplications AS
SELECT 
    af.application_form_id,
    af.ApplicantID,
    af.AdminID,
    af.email,
    af.first_name,
    af.last_name,
    af.date_of_birth,
    af.gender,
    af.twelfth_percentage,
    af.twelfth_year,
    af.twelfth_stream,
    af.university_name,
    af.test_schedule,
    af.test_score,
    af.status,
    af.fee_status,
    af.is_submitted,
    af.is_scholarship_submitted,
    af.programid,
    p.ProgramName,
    p.College_ID
FROM dbo.ApplicationForm af
LEFT JOIN dbo.Program p ON af.programid = p.ProgramID;
GO

-- ============================================================
-- COLLEGE & PROGRAM VIEWS
-- ============================================================

-- View: All Colleges with Programs and Allowed Streams
-- Purpose: Complex join to display college hierarchy with all programs and streams
-- Used by: CollegeManager.loadFromDatabase()
-- Joins: College -> Program -> ProgramStream -> Stream
CREATE VIEW vw_CollegesProgramsStreams AS
SELECT 
    c.college_id,
    c.college_name,
    p.ProgramID,
    p.ProgramName,
    p.Seats,
    p.Eligibility,
    p.Fee,
    s.stream_id,
    s.name AS StreamName
FROM dbo.College c
LEFT JOIN dbo.Program p ON c.college_id = p.College_ID
LEFT JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
LEFT JOIN dbo.Stream s ON ps.stream_id = s.stream_id;
GO

-- View: Colleges with Programs and Aggregated Streams
-- Purpose: Display colleges with programs and comma-separated stream names
-- Used by: CollegeAndProgramViewer_Panel.loadCollegeData()
-- Aggregates stream names using STRING_AGG for easy display
CREATE VIEW vw_CollegesProgramsAggregated AS
SELECT 
    c.college_id, 
    c.college_name,
    p.ProgramID, 
    p.ProgramName, 
    p.Seats, 
    p.Eligibility, 
    p.Fee,
    STRING_AGG(s.name, ', ') AS streams
FROM dbo.College c
LEFT JOIN dbo.Program p ON p.College_ID = c.college_id
LEFT JOIN dbo.ProgramStream ps ON ps.programid = p.ProgramID
LEFT JOIN dbo.Stream s ON s.stream_id = ps.stream_id
GROUP BY c.college_id, c.college_name, p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee;
GO

-- ============================================================
-- PROGRAM VIEWS
-- ============================================================

-- View: All Programs with Aggregated Streams
-- Purpose: Display all programs with comma-separated stream names
-- Used by: ProgramManager.getAllPrograms()
CREATE VIEW vw_ProgramsWithStreams AS
SELECT 
    p.ProgramID, 
    p.ProgramName, 
    p.College_ID,
    p.Seats, 
    p.Eligibility, 
    p.Fee,
    STRING_AGG(s.name, ', ') AS streams
FROM dbo.Program p
LEFT JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
LEFT JOIN dbo.Stream s ON ps.stream_id = s.stream_id
GROUP BY p.ProgramID, p.ProgramName, p.College_ID, p.Seats, p.Eligibility, p.Fee;
GO

-- View: Programs by Stream (without aggregation)
-- Purpose: Display programs associated with specific streams
-- Used by: ProgramManager.getProgramsByStream()
CREATE VIEW vw_ProgramsByStream AS
SELECT 
    p.ProgramID, 
    p.ProgramName, 
    p.College_ID,
    p.Seats, 
    p.Eligibility, 
    p.Fee,
    s.stream_id,
    s.name AS StreamName
FROM dbo.Program p
JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
JOIN dbo.Stream s ON ps.stream_id = s.stream_id;
GO

-- ============================================================
-- ENTRY TEST VIEWS
-- ============================================================

-- View: Entry Test Records with Paid Fee Status
-- Purpose: Display only test records for applicants who have paid fees
-- Used by: EntryTestRecordManager.loadAllRecords()
-- Joins EntryTestRecord with ApplicationForm to filter by fee status
CREATE VIEW vw_EntryTestRecordsPaid AS
SELECT 
    et.Application_Form_ID,
    et.TestDateTime,
    et.Passed,
    et.Score
FROM dbo.EntryTestRecord et
INNER JOIN dbo.ApplicationForm af ON et.Application_Form_ID = af.application_form_id
WHERE af.fee_status = 'PAID';
GO

-- ============================================================
-- END OF VIEWS
-- ============================================================
