create TABLE TVSeriesFollower.Series (
name varchar(50) NOT NULL,
latest_season int NOT NULL,
latest_episode int NOT NULL,
subtitles varchar(100),
primary key (name)
);

create TABLE TVSeriesFollower.Users (
address varchar(50) NOT NULL,
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

ALTER TABLE TVSeriesFollower.Series ADD COLUMN subtitles varchar(100);
