
    create table Book (
        id bigint not null,
        author varchar(255) not null,
        isbn varchar(13) not null,
        title varchar(255) not null,
        primary key (id)
    );

    create table book_lending (
        id bigint not null,
        borrowed_at date not null,
        due_date date not null,
        returned_at date,
        status varchar(20) not null check ((status in ('ACTIVE','RETURNED','OVERDUE'))),
        book_id bigint not null,
        member_id bigint not null,
        primary key (id)
    );

    create table Member (
        id bigint not null,
        email varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    );

    alter table if exists Book 
       drop constraint if exists UKbi5lx9jtv1f52idrmc0ck8ysx;

    alter table if exists Book 
       add constraint UKbi5lx9jtv1f52idrmc0ck8ysx unique (isbn);

    alter table if exists Member 
       drop constraint if exists UK9qv6yhjqm8iafto8qk452gx8h;

    alter table if exists Member 
       add constraint UK9qv6yhjqm8iafto8qk452gx8h unique (email);

    create sequence book_lending_SEQ start with 1 increment by 50;

    create sequence Book_SEQ start with 1 increment by 50;

    create sequence Member_SEQ start with 1 increment by 50;

    alter table if exists book_lending 
       add constraint FKkntmd7w3yodqn3iyw65ymgxqg 
       foreign key (book_id) 
       references Book;

    alter table if exists book_lending 
       add constraint FKauxbqevoics4fblbub2lx20ox 
       foreign key (member_id) 
       references Member;
