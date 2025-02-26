-- Insert test users
INSERT INTO users (username, password) VALUES 
('student1', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG'), -- password: 'password'
('student2', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG'),
('teacher1', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG'),
('admin', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG');

-- Insert courses
INSERT INTO courses (title, description, available, min_score) VALUES
('Java Programming Basics', 'Learn the fundamentals of Java programming language', true, 70),
('Advanced Web Development', 'Master modern web development techniques', true, 75),
('Data Science Fundamentals', 'Introduction to data analysis and machine learning', false, 80);

-- Assign user roles to courses
INSERT INTO user_course_roles (user_id, course_id, role) VALUES
(1, 1, 'STUDENT'), -- student1 enrolled in Java Programming
(1, 2, 'STUDENT'), -- student1 enrolled in Web Development
(2, 1, 'STUDENT'), -- student2 enrolled in Java Programming
(3, 1, 'TEACHER'), -- teacher1 teaches Java Programming
(3, 2, 'TEACHER'), -- teacher1 teaches Web Development
(3, 3, 'TEACHER'); -- teacher1 teaches Data Science

-- Insert tasks with different types
INSERT INTO tasks (title, description, type, course_id, correct_answer, max_score) VALUES
-- Java Programming tasks
('Java Variables Quiz', 'Select the correct data type for storing decimal numbers', 'MULTIPLE_CHOICE', 1, 'double', 10),
('Java Control Structures', 'Select all valid loop types in Java', 'CHECKBOX', 1, 'for,while,do-while', 15),
('Java OOP Concepts', 'Explain the principles of Object-Oriented Programming', 'WRITTEN', 1, NULL, 25),

-- Web Development tasks
('HTML Elements', 'Which tag is used for creating hyperlinks?', 'MULTIPLE_CHOICE', 2, 'a', 10),
('CSS Selectors', 'Select all valid CSS selectors', 'CHECKBOX', 2, 'class,id,tag,attribute', 15),
('JavaScript Project', 'Create a simple to-do list application using JavaScript', 'WRITTEN', 2, NULL, 30),

-- Data Science tasks
('Statistical Measures', 'Which measure represents the middle value in a dataset?', 'MULTIPLE_CHOICE', 3, 'median', 10),
('Data Visualization', 'Select all appropriate chart types for categorical data', 'CHECKBOX', 3, 'bar,pie,heatmap', 15),
('Machine Learning Analysis', 'Analyze the provided dataset using a classification algorithm', 'WRITTEN', 3, NULL, 40);

-- Insert some task submissions
INSERT INTO task_submissions (user_id, task_id, answer, score, submitted_at, graded_at, graded_by, automatically_graded) VALUES
-- Student1's submissions
(1, 1, 'double', 10, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NULL, true),
(1, 2, 'for,while,do-while', 15, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NULL, true),
(1, 3, 'Object-Oriented Programming is based on four main principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.', 20, NOW() - INTERVAL '4 days', NOW() - INTERVAL '2 days', 3, false),

-- Student2's submissions
(2, 1, 'float', 5, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NULL, true),
(2, 2, 'for,while', 10, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NULL, true),
(2, 3, 'OOP is a programming paradigm that uses objects and classes.', NULL, NOW() - INTERVAL '1 day', NULL, NULL, false);

-- Insert certificate requests with correct status
INSERT INTO certificate_requests (user_id, course_id, requested_at, status) VALUES
(1, 1, NOW() - INTERVAL '2 days', 'IN_PROGRESS'),
(2, 1, NOW() - INTERVAL '1 day', 'IN_PROGRESS');