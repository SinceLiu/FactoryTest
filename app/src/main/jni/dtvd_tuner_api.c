/*****************************************************************************/
/* MODEL    ：FOMA7                                                          */
/* BLOCK    ：DTVミドルウェア                                                */
/* APL NAME ：チューナドライバ                                               */
/* FILE NAME：dtvd_tuner_api.c                                               */
/* FILE EXP ：チューナドライバ ミドル向けAPI関数                             */
/*****************************************************************************/
/****** DTV2モデル変更履歴 ***************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   | 理由                      */
/*------------+---------+-------------+----------+---------------------------*/
/* 2007.02.20 | 00.01版 | FSI高田尚子 |          |新規(DTV2流用)             */
/* 2007.04.04 | 00.02版 | FSI廣松隆志 |1100060   |PTバグ対応                 */
/* 2007.04.11 | 00.03版 | FSI廣松隆志 |1511024   |OMT対応                    */
/* 2007.05.22 | 00.04版 | FSI渡辺彰宏 |1130152   |省電力をコンパイルSWで無効 */
/*****************************************************************************/
/****** Androidモデル変更履歴 ************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2010.08.12 | 01.00版 | SEC         |Laputa    |Android対応                */
/*****************************************************************************/
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>

/*$M #Rev.01.00 DEL-S Laputa */
/*$M #include "hmi_dtv.h"*/
/*$M #Rev.01.00 DEL-E Laputa */

#include "dtvd_tuner.h"                         /* ミドル向けヘッダファイル */
#include "dtvd_tuner_api.h"                     /* API用ヘッダファイル */

/*****************************************************************************/
/* 内部関数プロトタイプ宣言                                                  */
/*****************************************************************************/
static signed int dtvd_tuner_api_ioctl( unsigned int, unsigned long, signed int* );
static signed int dtvd_tuner_api_ioctl_retry( unsigned int, unsigned long, signed int* );
static signed int dtvd_tuner_api_ioctl_init_retry( unsigned int, unsigned long );
static void dtvd_tuner_api_ioctl_retry_mode_set( unsigned int, unsigned long );
#if 0 /*Laputa*/
static void dtvd_tuner_api_log( unsigned char,
                                unsigned long,
                                unsigned char,
                                unsigned long,
                                unsigned long,
                                unsigned long,
                                unsigned long,
                                unsigned long,
                                unsigned long,
                                unsigned long );
#endif /*Laputa*/
/* チューナ終了(初期化失敗リトライ時の終了) */
static signed int dtvd_tuner_end_retry( void );

/* チューナAPI情報 */
static DTVD_TUNER_API_INFO_t dtvd_tuner_api_info = {-1};

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_OPEN                                                */
/* ABSTRACT : チューナドライバオープン                                       */
/* FUNCTION : チューナドライバのオープン                                     */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_OPEN
(
    void
)
{
    signed int fd;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_OPEN");

    /* チューナドライバのオープン */
    DTVD_DEBUG_MSG_CALL("open");
    fd = open( "/dev/dtvtuner", O_RDWR );

    if( fd <= 0 )
    {
        /* ドライバオープンエラーの場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      fd, errno, 0, 0, 0, 0 );

        return;
    }

    /* ファイルディスクリプタを保持する */
    dtvd_tuner_api_info.fd = fd;

    DTVD_DEBUG_MSG_INFO( "fd = %d\n", dtvd_tuner_api_info.fd );

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_OPEN");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_CLOSE                                               */
/* ABSTRACT : チューナドライバクローズ                                       */
/* FUNCTION : チューナドライバのクローズ                                     */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_CLOSE
(
    void
)
{
    signed int ret;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_CLOSE");

    /* チューナドライバのクローズ */
    DTVD_DEBUG_MSG_CALL("close");
    ret = close( dtvd_tuner_api_info.fd );

    if( ret != D_DTVD_TUNER_OK )
    {
        /* ドライバクローズエラーの場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, errno, 0, 0, 0, 0 );

        return;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_CLOSE");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_PWRON                                               */
/* ABSTRACT : チューナ電源ON                                                 */
/* FUNCTION : チューナの電源ON                                               */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_PWRON
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_PWRON_t dtvd_tuner_req_pwron;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_PWRON");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_pwron.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チューナ電源ON */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_PWRON,                         /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_pwron,        /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* 電源ON失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, errno, 0, 0, 0, 0 );

        return;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_PWRON");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_INIT                                                */
/* ABSTRACT : チューナ初期化                                                 */
/* FUNCTION : チューナの初期化                                               */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   : 2010/08/12 SEC         Laputa  Android対応                     */
/*****************************************************************************/
signed int DTVD_TUNER_INIT
(
/*$M #Rev.01.00 CHG-S Laputa */
/*$M signed int      queue_id       *//* ddsyncのキューID */
    unsigned char   *pipename  /* 名前付きPIPE名 */
/*$M #Rev.01.00 CHG-E Laputa */
)
{
    signed int ret;
    DTVD_TUNER_REQ_INIT_t dtvd_tuner_req_init;
    signed int err_no;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_INIT");

/*$M #Rev.01.00 INS-S Laputa */
    /* NULLチェック */
    if( pipename == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }
/*$M #Rev.01.00 INS-E Laputa */

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_init.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

/*$M #Rev.01.00 CHG-S Laputa */
/*$M*/    /* ddsyncのキューIDを設定 */
/*$M     dtvd_tuner_req_init.queue_id = queue_id; */
    /* 名前付きPIPE名を初期化要求構造体（DTVD_TUNER_REQ_INIT_t)へコピーする。 */
    strncpy( (char*)dtvd_tuner_req_init.pipename, (char*)pipename, sizeof(dtvd_tuner_req_init.pipename) );
    dtvd_tuner_req_init.pipename[sizeof(dtvd_tuner_req_init.pipename)-1] = 0;
/*$M #Rev.01.00 CHG-E Laputa */

    /* チューナ初期化 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_INIT,                          /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_init,         /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        switch( err_no )
        {
            case EINTR:                          /* シグナル抜け */
                /* 通常ありえないパスのためリセット */
                DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_ETC,
                                              DTVD_TUNER_API,
                                              0, 0, 0, 0, 0, 0 );
                break;
            case EIO:                            /* ハード異常 */
                /* 初期化失敗リトライ処理 */
                ret = dtvd_tuner_api_ioctl_init_retry( D_DTVD_TUNER_REQ_INIT,
                                                       (unsigned long) &dtvd_tuner_req_init );
                break;
            case EBADF:                          /* 無効なファイル記述子 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
            default:                             /* それ以外 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
          }
    }

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_INIT");

    return ret;
}

/*$M #Rev.01.00 INS-S Laputa */
/*****************************************************************************/
/* MODULE   : DTVD_TUNER_READ_TSP                                            */
/* ABSTRACT : dtvd_tuner_read呼び出し関数                                    */
/* FUNCTION : dtvd_tuner_read呼び出し                                        */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK       成功                                     */
/*            D_DTVD_TUNER_NG       失敗                                     */
/* CREATE   : 2010/08/12 SEC         Laputa  Android対応                     */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_READ_TSP
(
void        *buffer,        /* TSパケット受信バッファ */
signed int  length          /* TSパケット受信バッファサイズ */
)
{
    int ret;
    
    /* NULLチェック */
    if( buffer == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* 受信バッファサイズ異常 */
    if( length == 0 )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, length, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* チューナドライバのREADを行いTSパケット受信を行う。 */
    ret = read( dtvd_tuner_api_info.fd, buffer, length );
    if( ret < 0 )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }

    return ret;
}
/*$M #Rev.01.00 INS-E Laputa */

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_TUNE_DTV                                            */
/* ABSTRACT : DTV選局                                                        */
/* FUNCTION : DTVの選局                                                      */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_TUNE_DTV
(
    unsigned long seq_id,           /* シーケンスID */
    DTVD_TUNER_CHANNEL_t channel,   /* チャンネル情報 */
    DTVD_TUNER_ADJUST_t adjust,     /* チャンネル調整情報 */
    unsigned char kind              /* 選局種別 */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_TUNE_t dtvd_tuner_req_tune;
/*$M #Rev.01.00 DEL-S Laputa */
/*$M HmiStatus result;    */
/*$M HmiDtvCh channel_no; */
/*$M #Rev.01.00 DEL-E Laputa */
    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_TUNE_DTV");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_tune.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* シーケンスIDの設定 */
    dtvd_tuner_req_tune.seq_id = seq_id;

    /* チャンネルチェック */
    if( ( channel.no < D_DTVD_TUNER_CHANNEL_MIN ) ||
        ( channel.no > D_DTVD_TUNER_CHANNEL_MAX ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      channel.no, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* チャンネル設定 */
    dtvd_tuner_req_tune.ch.no = channel.no;

    /* 中心セグメント設定 */
    dtvd_tuner_req_tune.ch.seg = 7;     /* 中心セグメントを固定値で設定 */

    /* 伝送モードチェック */
    if( adjust.mode > D_DTVD_TUNER_TSMODE_MODE3 )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      adjust.mode, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* 伝送モード設定 */
    dtvd_tuner_req_tune.adj.mode = adjust.mode;

    /* ガードインターバルチェック */
    if( adjust.gi > D_DTVD_TUNER_GI_1_4 )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      adjust.gi, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* ガードインターバル設定 */
    dtvd_tuner_req_tune.adj.gi = adjust.gi;

    /* 選局種別チェック */
    if( ( kind != D_DTVD_TUNER_TUNE_KIND_NORMAL ) &&
        ( kind != D_DTVD_TUNER_TUNE_KIND_SEARCH ) &&
        ( kind != D_DTVD_TUNER_TUNE_KIND_SCAN ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      kind, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* 選局種別設定 */
    dtvd_tuner_req_tune.kind = kind;

/*$M #Rev.01.00 DEL-S Laputa */
/*$M*/ /* カメラドライバに現在使用中のDTVチャネルを通知 */
/*$M    channel_no = ( unsigned int )channel.no;                           */
/*$M    DTVD_DEBUG_MSG_CALL("Hmi_Dtv_SetDTVch");                           */
/*$M    result = Hmi_Dtv_SetDTVch( D_HMI_TRUE,                             */        /* チャンネル設定有効 */
/*$M                               channel_no );                           */
/*$M                                                                       */
/*$M    if( result != D_HMI_STS_OK )                                       */
/*$M    {                                                                  */
/*$M      *//* 失敗の場合リセット */
/*$M        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_VAPI,    */
/*$M                                      DTVD_TUNER_API,                  */
/*$M                                      result, channel_no, 0, 0, 0, 0 );*/
/*$M                                                                       */
/*$M        return D_DTVD_TUNER_NG;                                        */
/*$M    }                                                                  */
/*$M #Rev.01.00 DEL-E Laputa */

    /* 選局 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_TUNE_DTV,                      /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_tune,         /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* 選局失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_TUNE_DTV");

    return ret;

}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_STOP                                                */
/* ABSTRACT : チューナ停止                                                   */
/* FUNCTION : チューナの停止                                                 */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_STOP
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_STOP_t dtvd_tuner_req_stop;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_STOP");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_stop.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チューナ停止 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_STOP,                          /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_stop,         /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* チューナ停止失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_STOP");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_END                                                 */
/* ABSTRACT : チューナ終了                                                   */
/* FUNCTION : チューナの終了                                                 */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_END
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_END_t dtvd_tuner_req_end;
/*$M #Rev.01.00 DEL-S Laputa */
/*$M    HmiStatus result;    */
/*$M    HmiDtvCh channel_no; */
/*$M #Rev.01.00 DEL-E Laputa */
    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_END");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_end.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* 終了種別(通常)に設定 */
    dtvd_tuner_req_end.kind = D_DTVD_TUNER_END_KIND_NORNAL;

    /* チューナ終了 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_END,                           /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_end,          /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* チューナ終了失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

/*$M #Rev.01.00 DEL-S Laputa */
/*$M *//* カメラドライバにチャンネル設定無効を通知 */
/*$M    channel_no = 0;                                                       */
/*$M    DTVD_DEBUG_MSG_CALL("Hmi_Dtv_SetDTVch");                              */
/*$M    result = Hmi_Dtv_SetDTVch( D_HMI_FALSE,                               */     /* チャンネル設定無効 */
/*$M                               channel_no );                              */
/*$M                                                                          */
/*$M    if( result != D_HMI_STS_OK )                                          */
/*$M    {                                                                     */
/*$M      *//* 失敗の場合リセット */
/*$M        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_VAPI,       */
/*$M                                      DTVD_TUNER_API,                     */
/*$M                                      result, channel_no, 0, 0, 0, 0 );   */
/*$M                                                                          */
/*$M       return D_DTVD_TUNER_NG;                                            */
/*$M    }                                                                     */
/*$M #Rev.01.00 DEL-E Laputa */

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_END");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_PWROFF                                              */
/* ABSTRACT : チューナ電源OFF                                                */
/* FUNCTION : チューナの電源OFF                                              */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_PWROFF
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_PWROFF_t dtvd_tuner_req_pwroff;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_PWROFF");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_pwroff.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チューナ電源OFF */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_PWROFF,                        /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_pwroff,       /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* 電源OFF失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_PWROFF");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_GET_CN                                              */
/* ABSTRACT : 受信レベル取得                                                 */
/* FUNCTION : 受信レベル（CN値）の取得                                       */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_GET_CN
(
    signed int *result,     /* 取得結果 */
    unsigned char *level    /* 受信レベル */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_GET_CN_t dtvd_tuner_req_get_cn;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_GET_CN");

    /* パラメータチェック */
    if( result == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return;
    }

    /* パラメータチェック */
    if( level == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );

        return;
    }

    /* 受信レベル取得 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_GET_CN,                        /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_get_cn,       /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* 受信レベル取得失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return;
    }

    /* 取得結果格納 */
    *result = dtvd_tuner_req_get_cn.result;

    /* 受信レベル格納 */
    *level = dtvd_tuner_req_get_cn.rx_level;

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_GET_CN");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_GET_INFO                                            */
/* ABSTRACT : チューナ情報取得                                               */
/* FUNCTION : チューナの情報取得                                             */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_GET_INFO
(
    DTVD_TUNER_MONITOR_INFO_t *info     /* チューナ情報構造体へのポインタ */
)

{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_GET_INFO_t dtvd_tuner_req_get_info;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_GET_INFO");

    /* パラメータチェック */
    if( info == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );

        return;
    }

    /* チューナ情報取得 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_GET_INFO,                      /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_get_info,     /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* チューナ情報取得失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return;
    }

    /* チューナ情報格納 */
    *info = dtvd_tuner_req_get_info.monitor;

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_GET_INFO");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_SET_STYLE                                           */
/* ABSTRACT : スタイル状態設定                                               */
/* FUNCTION : スタイル状態を設定する                                         */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_SET_STYLE
(
    unsigned int style      /* スタイル状態 */
)

{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_SET_STYLE_t dtvd_tuner_req_set_style;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_SET_STYLE");

    /* パラメータチェック */
   if( ( style != D_DTVD_TUNER_STYLE_NS ) &&
       ( style != D_DTVD_TUNER_STYLE_CS ) &&
       ( style != D_DTVD_TUNER_STYLE_VS ) &&
       ( style != D_DTVD_TUNER_STYLE_LLS ) &&
       ( style != D_DTVD_TUNER_STYLE_LRS ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      style, 0, 0, 0, 0, 0 );

        return;
    }

    /* スタイル状態格納 */
    dtvd_tuner_req_set_style.style = style;

    /* スタイル状態設定 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_SET_STYLE,                     /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_set_style,    /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* スタイル状態設定失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_SET_STYLE");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_SET_ECO                                             */
/* ABSTRACT : 省電力モード設定                                               */
/* FUNCTION : 省電力モードの設定                                             */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   : 2007/05/25 FSI渡辺彰宏 1130152                                 */
/*****************************************************************************/
void DTVD_TUNER_SET_ECO
(
    unsigned char mode      /* 省電力モード設定 */
)

{
/*$M #Rev.00.04 INS-S 1130152 */
#ifdef _DTVD_TUNER_ECO_ENABLE
/*$M #Rev.00.04 INS-E 1130152 */
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_SET_ECO_t dtvd_tuner_req_set_eco;
/*$M #Rev.00.04 INS-S 1130152 */
#endif /* _DTVD_TUNER_ECO_ENABLE */
/*$M #Rev.00.04 INS-E 1130152 */

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_SET_ECO");

    /* パラメータチェック */
    if( ( mode != D_DTVD_TUNER_ECO_MODE_OFF ) &&
        ( mode != D_DTVD_TUNER_ECO_MODE_ON ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      mode, 0, 0, 0, 0, 0 );

        return;
    }
/*$M #Rev.00.04 INS-S 1130152 */
#ifdef _DTVD_TUNER_ECO_ENABLE
/*$M #Rev.00.04 INS-E 1130152 */

    /* 省電力モードを設定 */
    dtvd_tuner_req_set_eco.eco_mode = mode;

    /* 省電力モード設定 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_SET_ECO,                       /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_set_eco,      /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* 省電力モード設定失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return;
    }
/*$M #Rev.00.04 INS-S 1130152 */
#endif /* _DTVD_TUNER_ECO_ENABLE */
/*$M #Rev.00.04 INS-E 1130152 */

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_SET_ECO");

    return;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_INIT_OMT                                            */
/* ABSTRACT : 工程用チューナ初期化                                           */
/* FUNCTION : 工程用のチューナ初期化                                         */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_INIT_OMT
(
    void
)
{
    signed int ret;
    DTVD_TUNER_REQ_INIT_OMT_t dtvd_tuner_req_init_omt;
    signed int err_no;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_INIT_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_init_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チューナ初期化 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_INIT_OMT,                      /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_init_omt,     /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        switch( err_no )
        {
            case EINTR:                          /* シグナル抜け */
                /* 通常ありえないパスのためリセット */
                DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_ETC,
                                              DTVD_TUNER_API,
                                              0, 0, 0, 0, 0, 0 );
                break;
            case EIO:                            /* ハード異常 */
                /* 初期化失敗リトライ処理 */
                ret = dtvd_tuner_api_ioctl_init_retry( D_DTVD_TUNER_REQ_INIT_OMT,
                                                       (unsigned long) &dtvd_tuner_req_init_omt );
                break;
            case EBADF:                          /* 無効なファイル記述子 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
            default:                             /* それ以外 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
          }
    }

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_INIT_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_STOP_OMT                                            */
/* ABSTRACT : 工程用チューナ停止                                             */
/* FUNCTION : 工程用のチューナ停止                                           */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_STOP_OMT
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_STOP_OMT_t dtvd_tuner_req_stop_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_STOP_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_stop_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チューナ停止 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_STOP_OMT,                      /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_stop_omt,     /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* チューナ停止失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_STOP_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_TUNE_DTV_OMT                                        */
/* ABSTRACT : 工程用DTV選局                                                  */
/* FUNCTION : 工程用のDTV選局                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_TUNE_DTV_OMT
(
    unsigned char ch_no,            /* 物理チャンネル */
    unsigned char sync,             /* 同期確認 */
    signed int* result,             /* 選局結果 */
    DTVD_TUNER_ADJUST_t* adjust     /* チャンネル調整情報 */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_TUNE_DTV_OMT_t dtvd_tuner_req_tune_omt;
/*$M #Rev.01.00 DEL-S Laputa */
/*$M    HmiStatus hmi_ret;   */
/*$M    HmiDtvCh channel_no; */
/*$M #Rev.01.00 DEL-E Laputa */

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_TUNE_DTV_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_tune_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* チャンネルチェック */
    if( ( ch_no < D_DTVD_TUNER_CHANNEL_MIN ) ||
        ( ch_no > D_DTVD_TUNER_CHANNEL_MAX ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      ch_no, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* チャンネル設定 */
    dtvd_tuner_req_tune_omt.ch_no = ch_no;

    /* 同期確認チェック */
    if( ( sync != D_DTVD_TUNER_DTVTUNE_SYNC_OFF ) &&
        ( sync != D_DTVD_TUNER_DTVTUNE_SYNC_ON ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      sync, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* 同期確認設定 */
    dtvd_tuner_req_tune_omt.sync = sync;

    /* パラメータチェック */
    if( result == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* パラメータチェック */
    if( adjust == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

/*$M #Rev.01.00 DEL-S Laputa */
/*$M */ /* カメラドライバに現在使用中のDTVチャネルを通知 */
/*$M    channel_no = ( unsigned int )ch_no;                                     */
/*$M    DTVD_DEBUG_MSG_CALL("Hmi_Dtv_SetDTVch");                                */
/*$M    hmi_ret = Hmi_Dtv_SetDTVch( D_HMI_TRUE,                                 */   /* チャンネル設定有効 */
/*$M                               channel_no );                                */
/*$M                                                                            */
/*$M    if( hmi_ret != D_HMI_STS_OK )                                           */
/*$M    {                                                                       */
/*$M      *//* 失敗の場合リセット */
/*$M        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_VAPI,         */
/*$M                                      DTVD_TUNER_API,                       */
/*$M                                      hmi_ret, channel_no, 0, 0, 0, 0 );    */
/*$M                                                                            */
/*$M        return D_DTVD_TUNER_NG;                                             */
/*$M    }                                                                       */
/*$M #Rev.01.00 DEL-E Laputa */

    /* 選局 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_TUNE_DTV_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_tune_omt,     /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* 選局失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;

        /* 同期確認設定が同期確認ありの場合のみ選局結果を格納 */
        if( sync == D_DTVD_TUNER_DTVTUNE_SYNC_ON )
        {
            /* 選局結果設定 */
            *result = dtvd_tuner_req_tune_omt.result;

            /* 選局結果が選局成功の場合のみモード、ガードインターバル格納 */
            if( *result == D_DTVD_TUNER_OK )
            {
                /* モード、ガードインターバル格納 */
                *adjust = dtvd_tuner_req_tune_omt.adj;
            }
        }
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_TUNE_DTV_OMT");

    return ret;

}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_BER_START_OMT                                       */
/* ABSTRACT : BER値測定開始                                                  */
/* FUNCTION : BER値測定の開始                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   : 2007/04/11 FSI 廣松 隆志 1511024                               */
/*****************************************************************************/
signed int DTVD_TUNER_BER_START_OMT
(
    unsigned char out,              /* 外部BER測定フラグ */
    unsigned char period            /* BER測定区間 */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_BER_START_OMT_t dtvd_tuner_req_ber_start_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_BER_START_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_ber_start_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* 外部BER測定フラグチェック */
    if( ( out != D_DTVD_TUNER_BER_MEASURE_OUT_ON ) &&
        ( out != D_DTVD_TUNER_BER_MEASURE_OUT_OFF ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      out, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* 外部BER測定フラグ設定 */
    dtvd_tuner_req_ber_start_omt.out = out;

    /* BER測定区間チェック */
/*$M #Rev.00.03 CHG-S 1511024 */
/*$M if( period > D_DTVD_TUNER_BER_MEASURE_BERLEN_8192 )*/
    if( period > D_DTVD_TUNER_BER_MEASURE_BERLEN_MAX )
/*$M #Rev.00.03 CHG-E 1511024 */
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      period, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* BER測定区間設定 */
    dtvd_tuner_req_ber_start_omt.period = (unsigned short)( D_DTVD_TUNER_BER_MEASURE_PACKET( period ) );

    /* BER値測定開始 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_BER_START_OMT,                 /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_ber_start_omt,/* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* BER値測定開始失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_BER_START_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_BER_STOP_OMT                                        */
/* ABSTRACT : BER値測定停止                                                  */
/* FUNCTION : BER値測定の停止                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_BER_STOP_OMT
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_BER_STOP_OMT_t dtvd_tuner_req_ber_stop_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_BER_STOP_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_ber_stop_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* BER値測定停止 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_BER_STOP_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_ber_stop_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* BER値測定停止失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_BER_STOP_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_BER_GET_OMT                                         */
/* ABSTRACT : BER値取得                                                      */
/* FUNCTION : BER値の取得                                                    */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_BER_GET_OMT
(
    DTVD_TUNER_MEASURE_VALUE_t* value       /* BER値格納ポインタ */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_BER_GET_OMT_t dtvd_tuner_req_ber_get_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_BER_GET_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_ber_get_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* パラメータチェック */
    if( value == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* BER値取得 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_BER_GET_OMT,                   /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_ber_get_omt,  /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* BER値取得失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;

        /* BER値格納 */
        *value = dtvd_tuner_req_ber_get_omt.value;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_BER_GET_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_CNR_START_OMT                                       */
/* ABSTRACT : C/N値測定開始                                                  */
/* FUNCTION : C/N値測定の開始                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_CNR_START_OMT
(
    unsigned char carrier           /* CN測定キャリア数 */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_CNR_START_OMT_t dtvd_tuner_req_cnr_start_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_CNR_START_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_cnr_start_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* CN測定キャリア数チェック */
    if( carrier > D_DTVD_TUNER_CN_MEASURE_CNWIN_8 )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      carrier, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    /* CN測定キャリア数設定 */
    dtvd_tuner_req_cnr_start_omt.carrier = carrier;

    /* C/N値測定開始 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_CNR_START_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_cnr_start_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* C/N値測定開始失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_CNR_START_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_CNR_STOP_OMT                                        */
/* ABSTRACT : C/N値測定停止                                                  */
/* FUNCTION : C/N値測定の停止                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_CNR_STOP_OMT
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_CNR_STOP_OMT_t dtvd_tuner_req_cnr_stop_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_CNR_STOP_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_cnr_stop_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* C/N値測定停止 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_CNR_STOP_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_cnr_stop_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* C/N値測定停止失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_CNR_STOP_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_CNR_GET_OMT                                         */
/* ABSTRACT : C/N値取得                                                      */
/* FUNCTION : C/N値の取得                                                    */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_CNR_GET_OMT
(
    DTVD_TUNER_MEASURE_VALUE_t* value_x,    /* ブランチXのCN値格納ポインタ */
    DTVD_TUNER_MEASURE_VALUE_t* value_y,    /* ブランチYのCN値格納ポインタ */
    DTVD_TUNER_MEASURE_VALUE_t* value_comp  /* 合成後のCN値格納ポインタ */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_CNR_GET_OMT_t dtvd_tuner_req_cnr_get_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_CNR_GET_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_cnr_get_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* パラメータチェック */
    if( value_x == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* パラメータチェック */
    if( value_y == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* パラメータチェック */
    if( value_comp == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* C/N値取得 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_CNR_GET_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_cnr_get_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* C/N値取得失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;

        /* ブランチXのCN値格納 */
        *value_x = dtvd_tuner_req_cnr_get_omt.value_x;

        /* ブランチYのCN値格納 */
        *value_y = dtvd_tuner_req_cnr_get_omt.value_y;

        /* 合成後のCN値格納 */
        *value_comp = dtvd_tuner_req_cnr_get_omt.value_comp;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_CNR_GET_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_AGC_START_OMT                                       */
/* ABSTRACT : AGC値測定開始                                                  */
/* FUNCTION : AGC値測定の開始                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_AGC_START_OMT
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_AGC_START_OMT_t dtvd_tuner_req_agc_start_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_AGC_START_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_agc_start_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* AGC値測定開始 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_AGC_START_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_agc_start_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* AGC値測定開始失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_AGC_START_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_AGC_STOP_OMT                                        */
/* ABSTRACT : AGC値測定停止                                                  */
/* FUNCTION : AGC値測定の停止                                                */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_AGC_STOP_OMT
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_AGC_STOP_OMT_t dtvd_tuner_req_agc_stop_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_AGC_STOP_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_agc_stop_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* AGC値測定停止 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_AGC_STOP_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_agc_stop_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* AGC値測定停止失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_AGC_STOP_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_AGC_GET_OMT                                         */
/* ABSTRACT : AGC値取得                                                      */
/* FUNCTION : AGC値の取得                                                    */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
signed int DTVD_TUNER_AGC_GET_OMT
(
    unsigned char* value_x,         /* ブランチXのAGC値格納ポインタ */
    unsigned char* value_y          /* ブランチYのAGC値格納ポインタ */
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_AGC_GET_OMT_t dtvd_tuner_req_agc_get_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_AGC_GET_OMT");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_agc_get_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* パラメータチェック */
    if( value_x == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* パラメータチェック */
    if( value_y == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_NG;
    }

    /* AGC値取得 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_AGC_GET_OMT,                  /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_agc_get_omt, /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* AGC値取得失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;

        /* ブランチXのAGC値格納 */
        *value_x = dtvd_tuner_req_agc_get_omt.value_x;

        /* ブランチYのAGC値格納 */
        *value_y = dtvd_tuner_req_agc_get_omt.value_y;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_AGC_GET_OMT");

    return ret;
}

/*****************************************************************************/
/* MODULE   : DTVD_TUNER_ANT_SET_OMT                                         */
/* ABSTRACT : アンテナ設定                                                   */
/* FUNCTION : RFの入力アンテナを設定する                                     */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/20 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
void DTVD_TUNER_ANT_SET_OMT
(
    unsigned char ant           /* アンテナ種別 */
)
{
    signed int                      ret;
    signed int                      err_no;
    DTVD_TUNER_REQ_ANT_SET_OMT_t    dtvd_tuner_req_ant_set_omt;

    DTVD_DEBUG_MSG_ENTER("DTVD_TUNER_ANT_SET_OMT");

    /* パラメータチェック */
    if( ( ant != D_DTVD_TUNER_ANT_KIND_WHP ) &&
        ( ant != D_DTVD_TUNER_ANT_KIND_HSJ ) )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      ant, 0, 0, 0, 0, 0 );
        return;
    }

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_ant_set_omt.mode = D_DTVD_TUNER_CALLMODE_NORMAL;
    /* アンテナ種別を格納 */
    dtvd_tuner_req_ant_set_omt.ant  = ant;
    /* アンテナ設定 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_ANT_SET_OMT,                   /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_ant_set_omt,  /* 要求データ   */
                                &err_no );

    if( ret != D_DTVD_TUNER_API_OK )
    {
        /* アンテナ設定失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );
        return;
    }

    DTVD_DEBUG_MSG_EXIT("DTVD_TUNER_ANT_SET_OMT");

    return;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_api_ioctl                                           */
/* ABSTRACT : ioctlラッパー関数                                              */
/* FUNCTION : ioctlラッパー関数                                              */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_API_OK         成功                               */
/*            D_DTVD_TUNER_API_NG_DEVERR  失敗（ハード異常）                 */
/*            D_DTVD_TUNER_API_NG_RESET   失敗（それ以外）                   */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   : 2007/04/04 FSI廣松隆志 1100060                                 */
/*****************************************************************************/
signed int dtvd_hardware_test_ioctl
(
    unsigned int  command,   
    unsigned long arg                  
)
{
    signed int ret = D_DTVD_TUNER_NG;

    ret = ioctl( dtvd_tuner_api_info.fd,         /* ファイルディスクリプタ */
                 command,                        /* 要求コマンド */
                 arg );                          /* 要求データ */
    return ret;
}


static signed int dtvd_tuner_api_ioctl
(
    unsigned int  command,      /* 要求コマンド */
    unsigned long arg,          /* 要求データ */
    signed   int  *err_no       /* errno */
)
{
    signed int ret;

    DTVD_DEBUG_MSG_ENTER("dtvd_tuner_api_ioctl");

    if( err_no == D_DTVD_TUNER_API_NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );

        return D_DTVD_TUNER_API_NG_RESET;
    }


    /* ioctl実行 */
    DTVD_DEBUG_MSG_CALL("ioctl");
    ret = ioctl( dtvd_tuner_api_info.fd,         /* ファイルディスクリプタ */
                 command,                        /* 要求コマンド */
                 arg );                          /* 要求データ */

    *err_no = errno;
    if( ret != D_DTVD_TUNER_OK )
    {
        switch( *err_no )
        {
            case EINTR:                          /* シグナル抜け */
                /* リトライ処理 */
/*$M #Rev.00.02 CHG-S 1100060 */
                ret = dtvd_tuner_api_ioctl_retry( command,
                                                  arg,
                                                  err_no );
/*$M #Rev.00.02 CHG-E 1100060 */
                break;
            case EIO:                            /* ハード異常 */
                ret = D_DTVD_TUNER_API_NG_DEVERR;
                break;
            case EBADF:                          /* 無効なファイル記述子 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
            default:                             /* それ以外 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
        }
    }
    else
    {
        *err_no = 0;
        ret = D_DTVD_TUNER_API_OK;
    }

    DTVD_DEBUG_MSG_EXIT("dtvd_tuner_api_ioctl");

    return ret;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_api_ioctl_retry                                     */
/* ABSTRACT : ioctlリトライラッパー関数                                      */
/* FUNCTION : ioctlリトライラッパー関数                                      */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_API_OK         成功                               */
/*            D_DTVD_TUNER_API_NG_DEVERR  失敗（ハード異常）                 */
/*            D_DTVD_TUNER_API_NG_RESET   失敗（それ以外のエラー）           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   : 2007/04/04 FSI廣松隆志 1100060                                 */
/*****************************************************************************/
static signed int dtvd_tuner_api_ioctl_retry
(
    unsigned int command,      /* 要求コマンド */
    unsigned long arg,         /* 要求データ */
/*$M #Rev.00.02 INS-S 1100060 */
    signed   int  *err_no      /* errno */
/*$M #Rev.00.02 INS-E 1100060 */
)
{
    signed int ret;
    signed int rty_count;

    DTVD_DEBUG_MSG_ENTER("dtvd_tuner_api_ioctl_retry");

    /* パラメータチェック */
    if( err_no == NULL )
    {
        /* リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_PARAM,
                                      DTVD_TUNER_API,
                                      0, 0, 0, 0, 0, 0 );
        return D_DTVD_TUNER_API_NG_RESET;
    }

    /* リトライモードに設定 */
    dtvd_tuner_api_ioctl_retry_mode_set( command,             /* 要求コマンド */
                                         arg );               /* 要求データ */

    /* カウンタの初期化 */
    rty_count = 0;

    /* リトライモードにてioctl実行 */
    DTVD_DEBUG_MSG_CALL("ioctl");
    ret = ioctl( dtvd_tuner_api_info.fd,         /* ファイルディスクリプタ */
                 command,                        /* 要求コマンド */
                 arg );                          /* 要求データ */

/*$M #Rev.00.02 CHG-S 1100060 */
    *err_no = errno;
    if( ret != D_DTVD_TUNER_OK )
    {
        switch( *err_no )
        {
            case EINTR:                          /* シグナル抜け */
                do
                {
                    /* シグナル抜けによる終了以外で終了するまで繰り返す */
                    if( rty_count < D_DTVD_TUNER_API_IOCTL_RETRY_COUNT )
                    {
                        DTVD_DEBUG_MSG_CALL("ioctl");
                        ret = ioctl( dtvd_tuner_api_info.fd,
                                     command,
                                     arg );
                        *err_no = errno;

                        rty_count++;
                    }
                    else
                    {
                        /* リセット */
                        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_ETC,
                                                      DTVD_TUNER_API,
                                                      ret, *err_no, 0, 0, 0, 0 );

                        break;
                    }
                }
                while( ( ret != D_DTVD_TUNER_OK ) && ( *err_no == EINTR ) );

                if( ( ret != D_DTVD_TUNER_OK ) && ( *err_no == EIO ) )
                {
                    /* ハード異常 */
                    ret = D_DTVD_TUNER_API_NG_DEVERR;
                }
                else if( ret != D_DTVD_TUNER_OK )
                {
                    ret = D_DTVD_TUNER_API_NG_RESET;
                }
                else
                {
                    ret = D_DTVD_TUNER_API_OK;
                }
                break;
            case EIO:                            /* ハード異常 */
                ret = D_DTVD_TUNER_API_NG_DEVERR;
                break;
            case EBADF:                          /* 無効なファイル記述子 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
            default:                             /* それ以外 */
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
        }
    }
    else
    {
        ret = D_DTVD_TUNER_API_OK;
    }
/*$M #Rev.00.02 CHG-E 1100060 */

    DTVD_DEBUG_MSG_EXIT("dtvd_tuner_api_ioctl_retry");

    return ret;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_api_ioctl_init_retry                                */
/* ABSTRACT : 初期化失敗リトライラッパー関数                                 */
/* FUNCTION : 初期化失敗リトライラッパー関数                                 */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_API_OK         成功                               */
/*            D_DTVD_TUNER_API_NG_DEVERR  失敗（ハード異常）                 */
/*            D_DTVD_TUNER_API_NG_RESET   失敗（それ以外のエラー）           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
static signed int dtvd_tuner_api_ioctl_init_retry
(
    unsigned int command,      /* 要求コマンド */
    unsigned long arg          /* 要求データ */
)
{
    signed int ret;
    signed int rty_count;
    signed int err_no;
    DTVD_TUNER_REQ_INIT_t *init;
    DTVD_TUNER_REQ_INIT_OMT_t *init_omt;

    DTVD_DEBUG_MSG_ENTER("dtvd_tuner_api_ioctl_init_retry");

    ret = D_DTVD_TUNER_API_OK;

    for( rty_count = 0; rty_count < D_DTVD_TUNER_API_INIT_RETRY_COUNT; rty_count++)
    {
        /* チューナ終了 */
        ret = dtvd_tuner_end_retry();
        if( ret != D_DTVD_TUNER_OK )
        {
            return D_DTVD_TUNER_API_NG_DEVERR;
        }

        /* チューナ電源OFF */
        DTVD_TUNER_PWROFF();

        /* チューナ電源ON */
        DTVD_TUNER_PWRON();

        /* 初期化実行 */
        switch ( command )
        {
            case D_DTVD_TUNER_REQ_INIT:                     /* 初期化 */
                init = ( DTVD_TUNER_REQ_INIT_t * )arg;
                init->mode = D_DTVD_TUNER_CALLMODE_NORMAL;
                break;
            case D_DTVD_TUNER_REQ_INIT_OMT:                 /* 工程用初期化 */
                init_omt = ( DTVD_TUNER_REQ_INIT_OMT_t * )arg;
                init_omt->mode = D_DTVD_TUNER_CALLMODE_NORMAL;
                break;
            default:
                /* リセット */
                DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                              DTVD_TUNER_API,
                                              command, 0, 0, 0, 0, 0 );

                return D_DTVD_TUNER_API_NG_RESET;
        }

        ret = dtvd_tuner_api_ioctl( command,  /* 要求コマンド */
                                    arg,      /* 要求データ */
                                    &err_no );

        if( ret != D_DTVD_TUNER_API_OK )
        {
            /* ハード異状の場合 */
            if( ret == D_DTVD_TUNER_API_NG_DEVERR )
            {
                ret = D_DTVD_TUNER_API_NG_DEVERR;
            }
            /* ハード異状以外の場合 */
            else
            {
                ret = D_DTVD_TUNER_API_NG_RESET;
                break;
            }
        }
        else
        {
            ret = D_DTVD_TUNER_API_OK;
            break;
        }
    }

    DTVD_DEBUG_MSG_EXIT("dtvd_tuner_api_ioctl_init_retry");

    return ret;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_api_ioctl_retry_mode_set                            */
/* ABSTRACT : ioctlリトライモード設定関数                                    */
/* FUNCTION : ioctlリトライモード設定関数                                    */
/* NOTE     :                                                                */
/* RETURN   : 要求データ                                                     */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
static void dtvd_tuner_api_ioctl_retry_mode_set
(
    unsigned int command,      /* 要求コマンド */
    unsigned long arg          /* 要求データ */
)
{
    DTVD_TUNER_REQ_PWRON_t *pwron;
    DTVD_TUNER_REQ_INIT_t *init;
    DTVD_TUNER_REQ_STOP_t *stop;
    DTVD_TUNER_REQ_END_t *end;
    DTVD_TUNER_REQ_PWROFF_t *pwroff;
    DTVD_TUNER_REQ_TUNE_t *tune;
    DTVD_TUNER_REQ_INIT_OMT_t *init_omt;
    DTVD_TUNER_REQ_STOP_OMT_t *stop_omt;
    DTVD_TUNER_REQ_TUNE_DTV_OMT_t *tune_omt;
    DTVD_TUNER_REQ_BER_START_OMT_t *ber_start_omt;
    DTVD_TUNER_REQ_BER_STOP_OMT_t *ber_stop_omt;
    DTVD_TUNER_REQ_BER_GET_OMT_t *ber_get_omt;
    DTVD_TUNER_REQ_CNR_START_OMT_t *cnr_start_omt;
    DTVD_TUNER_REQ_CNR_STOP_OMT_t *cnr_stop_omt;
    DTVD_TUNER_REQ_CNR_GET_OMT_t *cnr_get_omt;
    DTVD_TUNER_REQ_AGC_START_OMT_t *agc_start_omt;
    DTVD_TUNER_REQ_AGC_STOP_OMT_t *agc_stop_omt;
    DTVD_TUNER_REQ_AGC_GET_OMT_t *agc_get_omt;
    DTVD_TUNER_REQ_ANT_SET_OMT_t *ant_set_omt;

    DTVD_DEBUG_MSG_ENTER("dtvd_tuner_api_ioctl_retry_mode_set");

    /* 各要求に対し、リトライモードを設定 */
    switch ( command )
    {
        case D_DTVD_TUNER_REQ_PWRON:                    /* 電源ON */
            pwron = ( DTVD_TUNER_REQ_PWRON_t* ) arg;
            pwron->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_INIT:                     /* 初期化 */
            init = ( DTVD_TUNER_REQ_INIT_t* ) arg;
            init->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_STOP:                     /* 停止 */
            stop = ( DTVD_TUNER_REQ_STOP_t* ) arg;
            stop->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_END:                      /* 終了 */
            end = ( DTVD_TUNER_REQ_END_t* ) arg;
            end->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_PWROFF:                   /* 電源OFF */
            pwroff = ( DTVD_TUNER_REQ_PWROFF_t* ) arg;
            pwroff->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_TUNE_DTV:                 /* DTV選局 */
            tune = ( DTVD_TUNER_REQ_TUNE_t* ) arg;
            tune->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_INIT_OMT:                 /* 工程用初期化 */
            init_omt = ( DTVD_TUNER_REQ_INIT_OMT_t* ) arg;
            init_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_STOP_OMT:                 /* 工程用停止 */
            stop_omt = ( DTVD_TUNER_REQ_STOP_OMT_t* ) arg;
            stop_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_TUNE_DTV_OMT:             /* 工程用DTV選局 */
            tune_omt = ( DTVD_TUNER_REQ_TUNE_DTV_OMT_t* ) arg;
            tune_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_BER_START_OMT:            /* BER値測定開始 */
            ber_start_omt = ( DTVD_TUNER_REQ_BER_START_OMT_t* ) arg;
            ber_start_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_BER_STOP_OMT:             /* BER値測定停止 */
            ber_stop_omt = ( DTVD_TUNER_REQ_BER_STOP_OMT_t* ) arg;
            ber_stop_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_BER_GET_OMT:              /* BER値取得 */
            ber_get_omt = ( DTVD_TUNER_REQ_BER_GET_OMT_t* ) arg;
            ber_get_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_CNR_START_OMT:            /* C/N値測定開始 */
            cnr_start_omt = ( DTVD_TUNER_REQ_CNR_START_OMT_t* ) arg;
            cnr_start_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_CNR_STOP_OMT:             /* C/N値測定停止 */
            cnr_stop_omt = ( DTVD_TUNER_REQ_CNR_STOP_OMT_t* ) arg;
            cnr_stop_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_CNR_GET_OMT:              /* C/N値取得 */
            cnr_get_omt = ( DTVD_TUNER_REQ_CNR_GET_OMT_t* ) arg;
            cnr_get_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_AGC_START_OMT:            /* AGC値測定開始 */
            agc_start_omt = ( DTVD_TUNER_REQ_AGC_START_OMT_t* ) arg;
            agc_start_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_AGC_STOP_OMT:            /* AGC値測定停止 */
            agc_stop_omt = ( DTVD_TUNER_REQ_AGC_STOP_OMT_t* ) arg;
            agc_stop_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_AGC_GET_OMT:             /* AGC値取得 */
            agc_get_omt = ( DTVD_TUNER_REQ_AGC_GET_OMT_t* ) arg;
            agc_get_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        case D_DTVD_TUNER_REQ_ANT_SET_OMT:             /* 工程用アンテナ設定 */
            ant_set_omt = ( DTVD_TUNER_REQ_ANT_SET_OMT_t* ) arg;
            ant_set_omt->mode = D_DTVD_TUNER_CALLMODE_RETRY;
            break;
        default:
            /* リセット */
            DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                          DTVD_TUNER_API,
                                          command, 0, 0, 0, 0, 0 );
            return;
    }

    DTVD_DEBUG_MSG_EXIT("dtvd_tuner_api_ioctl_retry_mode_set");

    return;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_end_retry                                           */
/* ABSTRACT : チューナ終了(初期化失敗リトライ時の終了)                       */
/* FUNCTION : 初期化失敗後のリトライ時にチューナを終了する                   */
/* NOTE     :                                                                */
/* RETURN   : D_DTVD_TUNER_OK             成功                               */
/*            D_DTVD_TUNER_NG             失敗                               */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
static signed int dtvd_tuner_end_retry
(
    void
)
{
    signed int ret;
    signed int err_no;
    DTVD_TUNER_REQ_END_t dtvd_tuner_req_end;

    DTVD_DEBUG_MSG_ENTER("dtvd_tuner_end_retry");

    /* 呼び出しモード(通常)に設定 */
    dtvd_tuner_req_end.mode = D_DTVD_TUNER_CALLMODE_NORMAL;

    /* 終了種別(リトライ)に設定 */
    dtvd_tuner_req_end.kind = D_DTVD_TUNER_END_KIND_RETRY;

    /* チューナ終了 */
    ret = dtvd_tuner_api_ioctl( D_DTVD_TUNER_REQ_END,                           /* 要求コマンド */
                                ( unsigned long ) &dtvd_tuner_req_end,          /* 要求データ   */
                                &err_no );

    if( ret == D_DTVD_TUNER_API_NG_RESET )
    {
        /* チューナ終了失敗の場合リセット */
        DTVD_TUNER_API_SYSERR_RANK_A( D_DTVD_TUNER_SYSERR_API_SYSTEM,
                                      DTVD_TUNER_API,
                                      ret, err_no, 0, 0, 0, 0 );

        return D_DTVD_TUNER_NG;
    }

    if( ret == D_DTVD_TUNER_API_NG_DEVERR )
    {
        /* ハード異常 */
        ret = D_DTVD_TUNER_NG;
    }
    else
    {
        ret = D_DTVD_TUNER_OK;
    }

    DTVD_DEBUG_MSG_EXIT("dtvd_tuner_req_end");

    return ret;
}

/*****************************************************************************/
/* MODULE   : dtvd_tuner_api_log                                             */
/* ABSTRACT : syserr出力関数                                                 */
/* FUNCTION : syserrlib_log_write() のラッパー関数                           */
/* NOTE     :                                                                */
/* RETURN   : なし                                                           */
/* CREATE   : 2007/02/28 FSI高田尚子                                         */
/* UPDATE   :                                                                */
/*****************************************************************************/
 int close_app(void)
{
        close(dtvd_tuner_api_info.fd);
        return 0;
}


#if 0 /*Laputa*/
static void dtvd_tuner_api_log
(
    unsigned char   rank,       /* アラームランク */
    unsigned long   kind,       /* エラー種別 */
    unsigned char   file_no,    /* ファイル番号 */
    unsigned long   line,       /* 行番号 */
    unsigned long   data1,      /* data1 */
    unsigned long   data2,      /* data2 */
    unsigned long   data3,      /* data3 */
    unsigned long   data4,      /* data4 */
    unsigned long   data5,      /* data5 */
    unsigned long   data6       /* data6 */
)
{
    DTVD_TUNER_API_SYSERR_t err_log;
    WRITE_LOG_TYPE log_info;

    memset( &log_info, 0x00, sizeof(WRITE_LOG_TYPE) );
    memset( &err_log, 0x00, sizeof(DTVD_TUNER_API_SYSERR_t) );

    /* ランク、機能ID設定 */
    log_info.ubkind = rank;
    log_info.uifunc_no = ( D_DTVD_TUNER_SYSERR_BLKID | kind );

    /* フリーエリア設定 */
    err_log.file_no = file_no;
    err_log.line = line;
    err_log.log_data[0] = data1;
    err_log.log_data[1] = data2;
    err_log.log_data[2] = data3;
    err_log.log_data[3] = data4;
    err_log.log_data[4] = data5;
    err_log.log_data[5] = data6;

    memcpy( &log_info.log_dat, &err_log, CSYSERR_LOGINF_LEN );

    syserrlib_log_write( (unsigned long*)(void*)&log_info, sizeof(WRITE_LOG_TYPE) );
    return;
}
#endif /*Laputa*/

/*****************************************************************************/
/*   Copyright(C) 2006 Panasonic Mobile Communications Co.,Ltd.              */
/*****************************************************************************/
