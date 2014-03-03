#include <sqludf.h>

SQL_API_RC SQL_API_FN fake_filter0(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1)) {
        *outParm = 1;
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

SQL_API_RC SQL_API_FN fake_filter1(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *inParm1,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *inParmNullInd1,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1) && (*inParmNullInd1 != -1)) {     
        *outParm = 1;
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

SQL_API_RC SQL_API_FN fake_filter2(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *inParm1,
        SQLUDF_INTEGER *inParm2,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *inParmNullInd1,
        SQLUDF_NULLIND *inParmNullInd2,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1) && (*inParmNullInd1 != -1) && (*inParmNullInd2 != -1)) {
        *outParm = 1;
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

SQL_API_RC SQL_API_FN fake_filter3(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *inParm1,
        SQLUDF_INTEGER *inParm2,
        SQLUDF_INTEGER *inParm3,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *inParmNullInd1,
        SQLUDF_NULLIND *inParmNullInd2,
        SQLUDF_NULLIND *inParmNullInd3,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1) && (*inParmNullInd1 != -1) && (*inParmNullInd2 != -1) && (*inParmNullInd3 != -1)) {        
        *outParm = 1;
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

SQL_API_RC SQL_API_FN fake_filter4(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *inParm1,
        SQLUDF_INTEGER *inParm2,
        SQLUDF_INTEGER *inParm3,
        SQLUDF_INTEGER *inParm4,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *inParmNullInd1,
        SQLUDF_NULLIND *inParmNullInd2,
        SQLUDF_NULLIND *inParmNullInd3,
        SQLUDF_NULLIND *inParmNullInd4,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1) && (*inParmNullInd1 != -1) && (*inParmNullInd2 != -1) && (*inParmNullInd3 != -1) && (*inParmNullInd4 != -1)) {    
        *outParm = 1;
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

