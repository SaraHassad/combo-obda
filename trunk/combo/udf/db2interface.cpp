#include <sqludf.h>
#include "filter.h"

extern "C" SQL_API_RC SQL_API_FN is_real_tuple0(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {

    if ((*inParmNullInd0 != -1)) {
        int values[1];
        values[0] = *inParm0;
        *outParm = filter(values, 1);
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

extern "C" SQL_API_RC SQL_API_FN is_real_tuple1(
        SQLUDF_INTEGER *inParm0,
        SQLUDF_INTEGER *inParm1,
        SQLUDF_INTEGER *outParm,
        SQLUDF_NULLIND *inParmNullInd0,
        SQLUDF_NULLIND *inParmNullInd1,
        SQLUDF_NULLIND *outParmNullInd,
        SQLUDF_TRAIL_ARGS) {
    if ((*inParmNullInd0 != -1) && (*inParmNullInd1 != -1)) {
        int values[2];
        values[0] = *inParm0;
        values[1] = *inParm1;
        *outParm = filter(values, 2);
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

extern "C" SQL_API_RC SQL_API_FN is_real_tuple2(
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
        int values[3];
        values[0] = *inParm0;
        values[1] = *inParm1;
        values[2] = *inParm2;
        *outParm = filter(values, 3);
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

extern "C" SQL_API_RC SQL_API_FN is_real_tuple3(
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
        int values[4];
        values[0] = *inParm0;
        values[1] = *inParm1;
        values[2] = *inParm2;
        values[3] = *inParm3;
        *outParm = filter(values, 4);
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

extern "C" SQL_API_RC SQL_API_FN is_real_tuple4(
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
        int values[5];
        values[0] = *inParm0;
        values[1] = *inParm1;
        values[2] = *inParm2;
        values[3] = *inParm3;
        values[4] = *inParm4;
        *outParm = filter(values, 5);
        *outParmNullInd = 0;
    } else {
        *outParmNullInd = -1;
    }
    return (0);
}

