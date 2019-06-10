/*****************************************************************************/
/* MODEL    ：FOMA7                                                          */
/* BLOCK    ：DTVミドルウェア                                                */
/* APL NAME ：チューナドライバ                                               */
/* FILE NAME：dtvd_tuner.h                                                   */
/* FILE EXP ：チューナドライバ ミドル向けヘッダファイル                      */
/*****************************************************************************/
/****** FOMA7モデル変更履歴 **************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2007.02.20 | 00.01版 | FSI高田尚子 |          |新規(DTV2流用)             */
/*****************************************************************************/
/****** Androidモデル変更履歴 ************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2010.08.12 | 01.00版 | SEC         |Laputa    |Android対応                */
/*****************************************************************************/
#ifndef _DTVD_TUNER_
#define _DTVD_TUNER_

#include "dtvtuner.h"                                               /* チューナドライバのヘッダファイル */

/*****************************************************************************/
/* ミドル向けAPI関数定義                                                     */
/*****************************************************************************/
/* チューナドライバオープン */
void DTVD_TUNER_OPEN( void );

/* チューナドライバクローズ */
void DTVD_TUNER_CLOSE( void );

/* チューナ電源ON  */
void DTVD_TUNER_PWRON( void );

/* チューナ初期化 */

/*$M #Rev.01.00 CHG-S Laputa */
/*$M signed int DTVD_TUNER_INIT( signed int queue_id );            */ /* キューID(ddsync) */
signed int DTVD_TUNER_INIT( unsigned char *pipename );              /* 名前付きPIPE名 */
/*$M #Rev.01.00 CHG-E Laputa */

/*$M #Rev.01.00 INS-S Laputa */
/* dtvd_tuner_read呼び出し */
signed int DTVD_TUNER_READ_TSP(  void *buffer,                      /* TSパケット受信バッファ */
                                signed int length );                /* TSパケット受信バッファサイズ */
/*$M #Rev.01.00 INS-E Laputa */

/* DTV選局 */
signed int DTVD_TUNER_TUNE_DTV( unsigned long seq_id,               /* シーケンスID */
                                DTVD_TUNER_CHANNEL_t channel,       /* チャンネル情報 */
                                DTVD_TUNER_ADJUST_t adjust,         /* チャンネル調整情報 */
                                unsigned char kind );               /* 選局種別 */

/* チューナ停止 */
signed int DTVD_TUNER_STOP( void );

/* チューナ終了 */
signed int DTVD_TUNER_END( void );

/* チューナ電源OFF */
void DTVD_TUNER_PWROFF( void );

/* 受信レベル取得 */
void DTVD_TUNER_GET_CN( signed int *result,                         /* 取得結果 */
                        unsigned char *level );                     /* 受信レベル */

/* チューナ情報取得 */
void DTVD_TUNER_GET_INFO( DTVD_TUNER_MONITOR_INFO_t *info );        /* チューナモニタ情報 */

/* スタイル状態設定 */
void DTVD_TUNER_SET_STYLE( unsigned int style );                    /* スタイル状態 */

/* 省電力モード設定 */
void DTVD_TUNER_SET_ECO( unsigned char mode );                      /* 省電力モード設定 */

/*****************************************************************************/
/* 工程コマンド向けAPI関数定義                                               */
/*****************************************************************************/
/* 工程用チューナ初期化 */
signed int DTVD_TUNER_INIT_OMT( void );

/* 工程用チューナ停止 */
signed int DTVD_TUNER_STOP_OMT( void );

/* 工程用DTV選局 */
signed int DTVD_TUNER_TUNE_DTV_OMT( unsigned char ch_no,            /* 物理チャンネル */
                                    unsigned char sync,             /* 同期確認 */
                                    signed int* result,             /* 選局結果 */
                                    DTVD_TUNER_ADJUST_t* adjust );  /* チャンネル調整情報 */

/* BER値測定開始 */
signed int DTVD_TUNER_BER_START_OMT( unsigned char out,             /* 外部BER測定フラグ */
                                     unsigned char period );        /* BER測定区間 */

/* BER値測定停止 */
signed int DTVD_TUNER_BER_STOP_OMT( void );

/* BER値取得 */
signed int DTVD_TUNER_BER_GET_OMT( DTVD_TUNER_MEASURE_VALUE_t* value );         /* BER値格納ポインタ */

/* C/N値測定開始 */
signed int DTVD_TUNER_CNR_START_OMT( unsigned char carrier );       /* CN測定キャリア数 */

/* C/N値測定停止 */
signed int DTVD_TUNER_CNR_STOP_OMT( void );

/* C/N値取得 */
signed int DTVD_TUNER_CNR_GET_OMT( DTVD_TUNER_MEASURE_VALUE_t* value_x,         /* ブランチXのCN値格納ポインタ */
                                   DTVD_TUNER_MEASURE_VALUE_t* value_y,         /* ブランチYのCN値格納ポインタ */
                                   DTVD_TUNER_MEASURE_VALUE_t* value_comp );    /* 合成後のCN値格納ポインタ */

/* AGC値測定開始 */
signed int DTVD_TUNER_AGC_START_OMT( void );

/* AGC値測定停止 */
signed int DTVD_TUNER_AGC_STOP_OMT( void );

/* AGC値取得 */
signed int DTVD_TUNER_AGC_GET_OMT( unsigned char* value_x,          /* ブランチXのAGC値格納ポインタ */
                                   unsigned char* value_y );        /* ブランチYのAGC値格納ポインタ */

/* アンテナ設定  */
void DTVD_TUNER_ANT_SET_OMT( unsigned char ant );   /* アンテナ種別 */

/* アンテナ設定無効 */
#define DTVD_TUNER_ANT_DISABLE_OMT( a )    ( DTVD_TUNER_ANT_SET_OMT( D_DTVD_TUNER_ANT_KIND_HSJ ) )

#endif /* _DTVD_TUNER_ */
/*****************************************************************************/
/*   Copyright(C) 2007 Panasonic Mobile Communications Co.,Ltd.              */
/*****************************************************************************/
