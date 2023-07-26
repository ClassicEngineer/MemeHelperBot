CREATE TABLE IF NOT EXISTS picture (
    id SERIAL PRIMARY KEY,
    path varchar(500)
) ;

CREATE TABLE IF NOT EXISTS meme (
    name varchar(100) UNIQUE PRIMARY KEY,
    description text,
    picture_id SERIAL,
    CONSTRAINT photo_id FOREIGN KEY (picture_id) REFERENCES picture(id)
);

CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name varchar(150)
) ;


CREATE TABLE IF NOT EXISTS meme_categories (
    category_id SERIAL,
    meme_name varchar(100),
    FOREIGN KEY (meme_name) REFERENCES meme(name),
    FOREIGN KEY (category_id) REFERENCES category(id)
) ;
