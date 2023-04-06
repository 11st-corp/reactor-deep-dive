GRANT ALL PRIVILEGES ON *.* TO 'user'@'%';

DROP DATABASE IF EXISTS `todo`;

CREATE DATABASE todo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `todo`.`todo`;

# Todo Table
CREATE TABLE `todo`.`todo` (
   `id` INTEGER NOT NULL AUTO_INCREMENT,
   `content` TEXT NOT NULL,
   `created_date` DATETIME(6) NOT NULL DEFAULT NOW(6),
   `modified_date` DATETIME(6) NOT NULL DEFAULT NOW(6),
   PRIMARY KEY (`id`)
);