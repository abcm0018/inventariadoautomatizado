create table audit_scan_incidents
(
    id              int auto_increment
        primary key,
    timestamp       datetime    default CURRENT_TIMESTAMP       not null,
    employee_number varchar(50)                                 not null,
    sscc_attempt    varchar(255)                                null,
    ean_attempt     varchar(50)                                 null,
    batch_attempt   varchar(100)                                null,
    incident_type   varchar(50) default 'TIMEOUT_DAMAGED_LABEL' null,
    details         text                                        null
);

create table bulk_imports_jobs
(
    job_id            binary(16)                                            not null
        primary key,
    created_at        datetime(6)                                           not null,
    finished_at       datetime(6)                                           null,
    loader_type       enum ('CSV', 'JSON', 'XLSX')                          not null,
    original_filename varchar(255)                                          not null,
    result_details    text                                                  null,
    started_at        datetime(6)                                           null,
    status            enum ('COMPLETED', 'FAILED', 'PENDING', 'PROCESSING') not null,
    storage_path      varchar(255)                                          null,
    user_id           varchar(255)                                          null
);

create table email_log
(
    id            bigint auto_increment
        primary key,
    body          longtext                                              null,
    created_at    datetime(6)                                           not null,
    email_type    varchar(255)                                          not null,
    error_message text                                                  null,
    recipient     varchar(255)                                          not null,
    retry_count   int                                                   not null,
    sent_at       datetime(6)                                           null,
    status        enum ('BOUNCED', 'FAILED', 'PENDING', 'SENT', 'SPAM') not null,
    subject       varchar(255)                                          not null,
    updated_at    datetime(6)                                           null
);

create index idx_email_type
    on email_log (email_type);

create index idx_recipient
    on email_log (recipient);

create index idx_sent_at
    on email_log (sent_at);

create index idx_status
    on email_log (status);

create table email_template
(
    id            bigint auto_increment
        primary key,
    body          text         not null,
    subject       varchar(255) not null,
    template_name varchar(255) not null,
    constraint UK_5i6vgpwroouh0d34wvj9fsdig
        unique (template_name)
);

create table products
(
    id              bigint auto_increment
        primary key,
    brand           varchar(200)                                              not null,
    created_at      datetime(6)                                               not null,
    description     varchar(500)                                              null,
    format_code     varchar(255)                                              not null,
    manufactured_in varchar(255)                                              not null,
    name            varchar(255)                                              not null,
    status          enum ('ACTIVE', 'DISCONTINUED', 'PILOT', 'TEMPORARY_OUT') not null,
    updated_at      datetime(6)                                               null,
    constraint uk_brand_format_code
        unique (brand, format_code)
);

create table product_pack_levels
(
    id              bigint auto_increment
        primary key,
    boxes_per_palet int                             not null,
    created_at      datetime(6)                     not null,
    gtin            varchar(14)                     not null,
    height_mm       decimal(38, 2)                  not null,
    net_weight      decimal(38, 2)                  not null,
    packing_level   enum ('CASE', 'PALLET', 'UNIT') not null,
    stacking_limit  int                             not null,
    units_in_level  int                             not null,
    updated_at      datetime(6)                     null,
    width_mm        decimal(38, 2)                  not null,
    product_id      bigint                          not null,
    constraint uk_ppl_gtin
        unique (gtin),
    constraint fk_ppl_product
        foreign key (product_id) references products (id),
    check ((`boxes_per_palet` >= 0) and (`boxes_per_palet` <= 10000)),
    check (`height_mm` <= 3000),
    check (`net_weight` <= 1000000),
    check ((`stacking_limit` <= 20) and (`stacking_limit` >= 1)),
    check ((`units_in_level` >= 1) and (`units_in_level` <= 10000)),
    check (`width_mm` <= 3000)
);

create index idx_ppl_gtin
    on product_pack_levels (gtin);

create index idx_product_brand
    on products (brand);

create index idx_product_name
    on products (name);

create index idx_product_status
    on products (status);

create table shifts
(
    id          bigint auto_increment
        primary key,
    active      bit                                             not null,
    create_at   datetime(6)                                     not null,
    description varchar(200)                                    null,
    end_time    time(6)                                         not null,
    shift_type  enum ('AFTERNOON', 'MORNING', 'NIGHT', 'SPLIT') not null,
    start_time  time(6)                                         not null,
    update_at   datetime(6)                                     null,
    constraint UK_lw9e7i11nebcxe5xttp5gmvmk
        unique (shift_type)
);

create index idx_shift_type
    on shifts (shift_type);

create table users
(
    id                bigint auto_increment
        primary key,
    active            bit                                      not null,
    blocked           bit                                      not null,
    create_at         datetime(6)                              not null,
    email             varchar(150)                             not null,
    employee_number   varchar(20)                              not null,
    expired           bit                                      not null,
    job_position      varchar(100)                             not null,
    name              varchar(100)                             not null,
    password          varchar(255)                             not null,
    registration_date date                                     not null,
    role              enum ('ADMIN', 'OPERATOR', 'SUPERVISOR') not null,
    surname           varchar(100)                             not null,
    update_at         datetime(6)                              null,
    constraint UK_6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint UK_o8o2hie1kxq48ja9nx2tbpbcc
        unique (employee_number)
);

create index idx_active
    on users (active);

create index idx_email
    on users (email);

create index idx_employee_number
    on users (employee_number);

create index idx_role
    on users (role);

create table workshifts
(
    id          bigint auto_increment
        primary key,
    create_at   datetime(6) not null,
    date        date        not null,
    update_at   datetime(6) null,
    version     bigint      null,
    shift_id    bigint      not null,
    employee_id bigint      not null,
    constraint uk_user_date
        unique (employee_id, date),
    constraint FK9k9gik36ejnglm3qt5n6848b6
        foreign key (shift_id) references shifts (id),
    constraint FKs0ktyn4l8r10atqy2y1eu2wkx
        foreign key (employee_id) references users (id)
);

create table palets
(
    id                  bigint auto_increment
        primary key,
    batch_number        varchar(255) not null,
    create_at           datetime(6)  not null,
    packagin_date       date         not null,
    product_use_by_date date         not null,
    production_time     varchar(255) not null,
    scanned_at          datetime(6)  not null,
    sscc                varchar(18)  not null,
    update_at           datetime(6)  null,
    pack_level_id       bigint       not null,
    employee_number     varchar(20)  null,
    workshift_id        bigint       not null,
    constraint uk_palet_sscc
        unique (sscc),
    constraint FKbxrpgfqk5fxwollt0ymdrs6su
        foreign key (employee_number) references users (employee_number),
    constraint FKl2dchikugssws9xjlkcbknwsp
        foreign key (pack_level_id) references product_pack_levels (id),
    constraint FKr9c03vhbais69hb5k7000u4st
        foreign key (workshift_id) references workshifts (id)
);

create index idx_palet_batch
    on palets (batch_number);

create index idx_palet_created_at
    on palets (create_at);

create index idx_palet_expiry_date
    on palets (product_use_by_date);

create index idx_palet_pack_level
    on palets (pack_level_id);

create index idx_palet_packaging_date
    on palets (packagin_date);

create index idx_palet_sscc
    on palets (sscc);

create index idx_palet_user
    on palets (employee_number);

create index idx_palet_workshift
    on palets (workshift_id);

create table timesheet
(
    id           bigint auto_increment
        primary key,
    check_in_at  datetime(6)                                     not null,
    check_out_at datetime(6)                                     null,
    create_at    datetime(6)                                     not null,
    notes        varchar(500)                                    null,
    status       enum ('ANOMALY', 'CLOSED', 'CORRECTED', 'OPEN') null,
    update_at    datetime(6)                                     null,
    workshift_id bigint                                          not null,
    constraint FKcuopobtkv6ekwxc7skvoq73o9
        foreign key (workshift_id) references workshifts (id)
);

create index idx_check_in
    on timesheet (check_in_at);

create index idx_check_out
    on timesheet (check_out_at);

create index idx_workshift
    on timesheet (workshift_id);

create table workshift_swap_request
(
    id                     bigint auto_increment
        primary key,
    create_at              datetime(6)                                           not null,
    reason                 varchar(500)                                          not null,
    review_notes           varchar(500)                                          null,
    reviewed_at            datetime(6)                                           null,
    status                 enum ('APPROVED', 'CANCELLED', 'PENDING', 'REJECTED') not null,
    update_at              datetime(6)                                           null,
    current_workshift_id   bigint                                                not null,
    requested_workshift_id bigint                                                not null,
    reviewed_by_user_id    bigint                                                null,
    user_id                bigint                                                not null,
    constraint FK392ncj7120g20i7dsbe4edxtk
        foreign key (current_workshift_id) references workshifts (id),
    constraint FK8qwmovgn26mqqsroxw5yfa2gc
        foreign key (requested_workshift_id) references workshifts (id),
    constraint FKovfpjiw2s07kl1u2ubgxfb2qs
        foreign key (reviewed_by_user_id) references users (id),
    constraint FKs8a0dreoqgtf8npo0jkudprsn
        foreign key (user_id) references users (id)
);

create index idx_create_at
    on workshift_swap_request (create_at);

create index idx_current_workshift
    on workshift_swap_request (current_workshift_id);

create index idx_requested_workshift
    on workshift_swap_request (requested_workshift_id);

create index idx_status
    on workshift_swap_request (status);

create index idx_user
    on workshift_swap_request (user_id);

create index idx_date
    on workshifts (date);

create index idx_shift_date
    on workshifts (shift_id, date);

create index idx_user_date
    on workshifts (employee_id, date);