INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'desc1',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme2', 'desc2',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme3', 'desc3',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at)
VALUES ('10:00');

INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 3);
