create TABLE TVSeriesFollower.Series (
name varchar(50) NOT NULL,
latest_season int NOT NULL,
latest_episode int NOT NULL,
subtitles varchar(100),
primary key (name)
);

create TABLE TVSeriesFollower.Users (
address varchar(50) NOT NULL,
password varchar(50) NOT NULL,
salt varchar(50) NOT NULL,
verified int DEFAULT 0 NOT NULL, 
primary key (address)
);

create TABLE TVSeriesFollower.UsersSeries (
usersSeries_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
address varchar(50) NOT NULL,
name varchar(50) NOT NULL,
primary key (usersSeries_id),
foreign key (address) references TVSeriesFollower.Users (address),
foreign key (name) references TVSeriesFollower.Series (name)
);

create TABLE TVSeriesFollower.Verifications (
verification_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
address varchar(50) NOT NULL,
verification_code varchar(50) NOT NULL,
creation_time timestamp NOT NULL,
primary key (verification_id)
);

create TABLE TVSeriesFollower.Passwordresets (
passwordreset_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
address varchar(50) NOT NULL,
token varchar(100) NOT NULL,
valid_until_time timestamp NOT NULL,
primary key (passwordreset_id),
foreign key (address) references TVSeriesFollower.Users (address)
);