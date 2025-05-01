CREATE DATABASE ProjectManagementDB
GO
USE ProjectManagementDB
GO

CREATE TABLE Role (
  Role_id VARCHAR(10) PRIMARY KEY NOT NULL,
  Description NVARCHAR(100)
)

CREATE TABLE User_
(
	User_id VARCHAR(20) PRIMARY KEY,
	User_name NVARCHAR(100), 
	Work_area NVARCHAR(30),
	Role_id VARCHAR(10) FOREIGN KEY REFERENCES Role(Role_id)
)

CREATE TABLE Project
(
	Project_id VARCHAR(20) PRIMARY KEY NOT NULL,
	Project_name VARCHAR(20), 
	Start_date DATE,
	End_date DATE,
	Status_project VARCHAR(20)

)

CREATE TABLE Phase
(
	Phase_id VARCHAR(10) PRIMARY KEY NOT NULL,
	Phase_name VARCHAR(20), 
	Description NVARCHAR(100)

)
CREATE TABLE Task
(
	Task_id VARCHAR(20) NOT NULL,
	Project_id VARCHAR(20) FOREIGN KEY REFERENCES Project (Project_id), 
	User_id VARCHAR(20) FOREIGN KEY REFERENCES User_ (User_id),
	Task_name VARCHAR(50),
	Phase_id VARCHAR(10) FOREIGN KEY REFERENCES Phase (Phase_id),
	Description NVARCHAR(500),
	Time_Plan TIME,
	Time_Actual TIME,
	Status_task VARCHAR(50),
	CONSTRAINT PK_Task PRIMARY KEY (Task_id, Project_id),
)

INSERT INTO Role
VALUES
('Dev',N'Developer: Người phát triển phần mềm'),
('Tester',N'Tester: Người kiểm thử phần mềm'),
('TeamLeader',N'TeamLeader: Leader của những Dev'),
('TestLeader',N'TestLeader: Leader của những Tester'),
('PM',N'PM: Người quản lí dự án')


INSERT INTO User_
VALUES
('HaNT01',N'Nguyễn Thu Hà', N'Tầng 2', 'Dev'),
('HungNT01',N'Nguyễn Trung Hưng', N'Tầng 2', 'Dev'),
('HaoNT01',N'Nguyễn Thu Hảo', N'Tầng 2', 'Tester'),
('HuyenNT01',N'Nguyễn Thu Huyền', N'Tầng 2', 'Tester'),
('DungNT01',N'Nguyễn Thùy Dung', N'Tầng 2', 'TestLeader'),
('MinhNT',N'Nguyễn Tiến Minh', N'Tầng 2', 'TeamLeader'),
('ChungNTK',N'Nguyễn Thị Kim Chung', N'Tầng 2', 'PM')

INSERT INTO Project
VALUES
('NAN_01','Nanoone_01', '2017-12-17', '2017-12-18', 'Open'),
('NAO_01','NaOone_01', '2017-12-15', '2017-12-16', 'Closed'),
('NAO_02','NaOone_02', '2017-12-15', '2017-12-16', 'Closed'),
('NAN_02','Nanoone_02', '2015-06-18', '2017-12-18', 'Open')

INSERT INTO Phase
VALUES
('BD','Basic design', N'Giai đoạn làm tài liệu'),
('CD','Coding', N'Giai đoạn code'),
('TEST','Testing', N'Giai đoạn kiểm thử phần mềm')

INSERT INTO Task
VALUES
('Task_01','NAN_01', 'HaNT01', 'Create_BD_Ra01', 'BD', N'Làm tài liệu Design cho chức năng Ra01',6,6,'Finished'),
('Task_02','NAN_01', 'HaNT01', 'Create_BD_Ra01', 'CD', N'Thực hiện code cho chức năng Ra01',2,6,'In-progress'),
('Task_03','NAN_01', 'HaoNT01', 'Create_TEST_Ra01', 'TEST', N'Kiểm thử cho chức năng Ra01',12,0,'Not yet start')

--Q3: List information for Users whose User_name includes 2 characters “AN” (located in any position) and is at least 6 characters long  

SELECT *
FROM User_
WHERE User_name LIKE '%AN%' AND LEN(User_name) >= 6;

--Q4: 
SELECT User_.User_name
FROM Role
JOIN User_ ON Role.Role_id = User_.Role_id
JOIN Task ON User_.User_id = Task.User_id
WHERE (User_.Role_id = 'TeamLeader' AND Task.Status_task = 'In-progress')
OR (User_.Role_id = 'Dev' AND Task.Status_task = 'Not yet start');

SELECT * FROM Project
--Q5:
SELECT Project.Project_id, Project.Project_name, 
Project.Start_date, Project.End_date, 
Project.Status_project, User_.User_name, User_.role_id
FROM User_
JOIN Task ON User_.User_id = Task.Task_id
JOIN Project ON Task.Task_id = Project.Project_id
WHERE DATEDIFF(MONTH, Project.Start_date, Project.End_date) > 6
AND YEAR(Project.Start_date) = 2022;

--Q6:
SELECT User_.User_name, Role.Role_id, Task.Task_name, Task.Status_task, 
Project.Project_name, Project.Start_date, Project.End_date, Project.Status_project
FROM Role
JOIN User_ ON Role.Role_id = User_.Role_id
JOIN Task ON User_.User_id = Task.Task_id
JOIN Project ON Task.Task_id = Project.Project_id
WHERE Project.Status_project = 'Open' AND YEAR(Project.Start_date) = 2022
ORDER BY Project.Start_date ASC;

--Q7:
SELECT User_.User_name
FROM User_
JOIN Task ON User_.User_id = Task.Task_id
JOIN Phase ON Task.Phase_id = Phase.Phase_id
WHERE Phase.Phase_name = 'Coding'
EXCEPT
SELECT User_.User_name
FROM User_
JOIN Task ON User_.User_id = Task.Task_id
JOIN Phase ON Task.Phase_id = Phase.Phase_id
WHERE Phase.Phase_name = 'Testing'

--Q8:
SELECT DISTINCT Task_name
FROM Task;

--Q9: 
SELECT User_.User_name, Role.Role_id, Task.Task_name, 
Phase.Phase_name, Project.Project_name, Project.Start_date, Project.End_date, Project.Status_project
FROM Role
JOIN User_ ON Role.Role_id = User_.Role_id
LEFT JOIN Task ON User_.User_id = Task.User_id
LEFT JOIN Phase ON Task.Phase_id = Phase.Phase_id
LEFT JOIN Project ON Task.Project_id = Project.Project_id;

--Q10: **??

UPDATE Task
SET Task.Status_task = 'pending'
FROM User_
JOIN Task ON User_.User_id = Task.User_id
JOIN Project ON Task.Project_id = Project.Project_id
WHERE Task.Status_task = 'Not yet start'
AND Project.Start_date BETWEEN '2023-10-01' AND '2023-12-31';

--Q11:
CREATE VIEW V_TASK AS
SELECT Project_id, Task_name, Time_plan
FROM Task;

--Q12:
CREATE INDEX ID_TenTask ON TASK (Task_name);

--Q13:
UPDATE V_TASK
SET Time_plan = Time_plan * 1.05;

--Q14: **

CREATE PROCEDURE SPR_01
AS
SELECT User_.User_id, Task.Task_name, Project.End_date
FROM User_
JOIN Task ON User_.User_id = Task.User_id
JOIN Project ON Task.Project_id = Project.Project_id
WHERE Task.Status_task = 'Completed' AND Project.End_date < DATEADD(hour, Task.Time_Actual, Project.Start_date);


