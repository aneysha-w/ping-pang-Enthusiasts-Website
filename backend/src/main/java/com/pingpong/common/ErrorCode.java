package com.pingpong.common;

public class ErrorCode {
    public static final int PHONE_EXISTS = 40001;
    public static final int SMS_CODE_ERROR = 40002;
    public static final int AVATAR_UPLOAD_FAIL = 40003;
    public static final int SMS_LIMIT_EXCEED = 40004;
    public static final int ACCOUNT_NOT_FOUND = 40005;
    public static final int PASSWORD_ERROR = 40006;
    public static final int PROFILE_INVALID = 40007;
    public static final int NICKNAME_SENSITIVE = 40008;
    public static final int FILE_FORMAT_ERROR = 40009;
    public static final int USER_NOT_FOUND = 40010;

    public static final int EVENT_STATUS_INVALID = 50001;
    public static final int NO_PERMISSION_RESULT = 50002;
    public static final int SKILL_LEVEL_INVALID = 50003;

    public static final int EVENT_FULL = 60001;
    public static final int EVENT_PLAYERS_NOT_ENOUGH = 60002;
    public static final int EVENT_FIELD_INVALID = 60003;
    public static final int EVENT_PLAYERS_NOT_POWER_OF_2 = 60004;
    public static final int EVENT_DEADLINE_INVALID = 60005;
    public static final int PROFILE_NOT_COMPLETE = 60006;
    public static final int CLUB_MEMBER_ONLY = 60007;
    public static final int ENROLL_CLOSED = 60008;
    public static final int NOT_ENROLLED = 60009;
    public static final int EVENT_STATUS_NOT_ALLOW_SCHEDULE = 60010;
    public static final int EVENT_STATUS_TRANSITION_INVALID = 60011;
    public static final int EVENT_STARTED_CANNOT_CANCEL = 60012;

    public static final int CLUB_NAME_EXISTS = 70001;
    public static final int NOT_CLUB_MEMBER = 70002;
    public static final int CONTENT_SENSITIVE = 70003;
    public static final int NO_PERMISSION_EDIT_POST = 70004;
    public static final int CLUB_HAS_ACTIVE_EVENT = 70005;

    public static final int ADMIN_PASSWORD_ERROR = 80001;
    public static final int REQUEST_ALREADY_PROCESSED = 80002;
    public static final int NOT_SYSTEM_ADMIN = 80003;
    public static final int REQUEST_NOT_FOUND = 80004;

    public static final int SMS_CODE_INVALID = 90001;
    public static final int SYSTEM_ERROR = 90000;
}