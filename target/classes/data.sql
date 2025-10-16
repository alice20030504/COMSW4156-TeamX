-- Sample data for development/testing
-- NOTE: Currently only PersonSimple and ApiLog entities exist
-- The following data is commented out until corresponding entities are created

-- Insert sample persons_simple (for the existing PersonSimple entity)
INSERT INTO persons_simple (name, birth_date, weight, height)
VALUES
    ('John Smith', '1990-05-15', 80.5, 180),
    ('Sarah Johnson', '1995-08-22', 65.0, 165),
    ('Mike Chen', '1988-03-10', 75.0, 175);

-- TODO: Uncomment when Exercise entity is created
-- INSERT INTO exercises (name, description, type, met_value, primary_muscle_group, difficulty, equipment_needed)
-- VALUES
--     ('Running', 'Outdoor or treadmill running', 'AEROBIC', 8.0, 'FULL_BODY', 'BEGINNER', 'None'),
--     ('Bench Press', 'Barbell bench press', 'ANAEROBIC', 6.0, 'CHEST', 'INTERMEDIATE', 'Barbell, Bench'),
--     ('Squats', 'Bodyweight or weighted squats', 'ANAEROBIC', 5.5, 'LEGS', 'BEGINNER', 'None or Barbell'),
--     ('Cycling', 'Stationary or outdoor cycling', 'AEROBIC', 7.5, 'LEGS', 'BEGINNER', 'Bicycle'),
--     ('Yoga', 'General yoga practice', 'FLEXIBILITY', 3.0, 'FULL_BODY', 'BEGINNER', 'Yoga Mat'),
--     ('Deadlifts', 'Barbell deadlifts', 'ANAEROBIC', 6.5, 'BACK', 'ADVANCED', 'Barbell'),
--     ('Push-ups', 'Standard push-ups', 'ANAEROBIC', 3.8, 'CHEST', 'BEGINNER', 'None'),
--     ('Swimming', 'Freestyle swimming', 'AEROBIC', 8.5, 'FULL_BODY', 'INTERMEDIATE', 'Swimming Pool');

-- TODO: Uncomment when Food entity is created
-- INSERT INTO foods (name, description, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size, serving_unit, food_category)
-- VALUES
--     ('Chicken Breast', 'Grilled skinless chicken breast', 165, 31, 0, 3.6, 0, 0, 74, 100, 'g', 'PROTEIN'),
--     ('Brown Rice', 'Cooked brown rice', 112, 2.6, 23.5, 0.9, 1.8, 0.4, 5, 100, 'g', 'CARBOHYDRATE'),
--     ('Broccoli', 'Steamed broccoli', 35, 2.8, 7.2, 0.4, 2.6, 1.7, 33, 100, 'g', 'VEGETABLE'),
--     ('Greek Yogurt', 'Plain Greek yogurt', 100, 10, 3.6, 5, 0, 3.6, 50, 150, 'g', 'DAIRY'),
--     ('Banana', 'Medium banana', 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1, 100, 'g', 'FRUIT'),
--     ('Almonds', 'Raw almonds', 579, 21.2, 21.6, 49.9, 12.5, 4.4, 1, 100, 'g', 'FAT'),
--     ('Salmon', 'Grilled Atlantic salmon', 208, 22.1, 0, 12.4, 0, 0, 59, 100, 'g', 'PROTEIN'),
--     ('Oatmeal', 'Rolled oats, cooked', 71, 2.5, 12, 1.5, 1.7, 0.3, 3, 100, 'g', 'GRAIN'),
--     ('Egg', 'Large egg, boiled', 155, 12.6, 1.1, 10.6, 0, 1.1, 124, 100, 'g', 'PROTEIN'),
--     ('Protein Shake', 'Whey protein shake', 120, 25, 3, 1, 0, 2, 100, 250, 'ml', 'SUPPLEMENT');

-- TODO: Uncomment when Person entity is created (currently only PersonSimple exists)
-- INSERT INTO persons (name, birth_date, weight, height, gender, objective, weekly_training_freq, body_fat, created_at, updated_at)
-- VALUES
--     ('John Smith', '1990-05-15', 80.5, 180, 'MALE', 'BULK', 4, 15.5, CURRENT_DATE, CURRENT_DATE),
--     ('Sarah Johnson', '1995-08-22', 65.0, 165, 'FEMALE', 'CUT', 5, 22.0, CURRENT_DATE, CURRENT_DATE),
--     ('Mike Chen', '1988-03-10', 75.0, 175, 'MALE', 'RECOVER', 3, 18.0, CURRENT_DATE, CURRENT_DATE),
--     ('Emily Davis', '1992-11-30', 58.0, 160, 'FEMALE', 'CUT', 6, 25.0, CURRENT_DATE, CURRENT_DATE),
--     ('Alex Wilson', '1998-07-18', 70.0, 170, 'OTHER', 'BULK', 4, 20.0, CURRENT_DATE, CURRENT_DATE);

-- TODO: Uncomment when Goal entity is created
-- INSERT INTO goals (person_id, goal_type, description, target_value, current_value, unit, start_date, target_date, status)
-- VALUES
--     (1, 'WEIGHT_GAIN', 'Gain 5kg of muscle mass', 85.5, 80.5, 'kg', CURRENT_DATE, DATEADD('MONTH', 3, CURRENT_DATE), 'IN_PROGRESS'),
--     (2, 'BODY_FAT_REDUCTION', 'Reduce body fat to 18%', 18.0, 22.0, '%', CURRENT_DATE, DATEADD('MONTH', 2, CURRENT_DATE), 'IN_PROGRESS'),
--     (4, 'WEIGHT_LOSS', 'Lose 5kg', 53.0, 58.0, 'kg', CURRENT_DATE, DATEADD('MONTH', 2, CURRENT_DATE), 'IN_PROGRESS');