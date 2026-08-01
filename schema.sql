-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL
    );

-- Справочник рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa_ratings (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
    );

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL CHECK (duration > 0),
    mpa_id INT NOT NULL,
    FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id)
    );

-- Справочник жанров
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
    );

-- Связь фильмов и жанров (Many-to-Many)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
    );

-- Таблица дружбы
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('UNCONFIRMED', 'CONFIRMED')),
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Таблица лайков
CREATE TABLE IF NOT EXISTS likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );