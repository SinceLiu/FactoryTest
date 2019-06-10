/*****************************************************************************/
/* MODEL    ：FOMA7                                                          */
/* BLOCK    ：DTVミドルウェア                                                */
/* APL NAME ：DTVチューナドライバ                                            */
/* FILE NAME：dtvtuner.h                                                     */
/* FILE EXP ：DTVチューナドライバ 外部向けヘッダファイル                     */
/*****************************************************************************/
/****** FOMA7モデル変更履歴 **************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2007.02.20 | 00.01版 | FSI高田尚子 |          |新規(DTV2流用)             */
/* 2007.04.11 | 00.02版 | FSI廣松隆志 | 1511024  |OMT対応                    */
/* 2008.07.10 | 00.03版 | PMC末吉広貴 |          |Linux2.6対応               */
/*****************************************************************************/
/****** D92CS1モデル変更履歴 *************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2009.06.11 | 00.04版 | PMC牧野崇弘 |          |母体指摘に対するRuleC対応  */
/*****************************************************************************/
/****** D01SW1モデル変更履歴 *************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2010.01.12 | 00.05版 | PMC牧野崇弘 |          |スイーベルスタイル定義追加 */
/*****************************************************************************/
/****** Androidモデル変更履歴 ************************************************/
/* 年月日     |Rev.No   |名前         |KeyWord   |理由                       */
/*------------+---------+-------------+----------+---------------------------*/
/* 2010.08.12 | 01.00版 | SEC         |Laputa    |Android対応                */
/*****************************************************************************/

#ifndef _DTVTUNER_H_
#define _DTVTUNER_H_

/*****************************************************************************/
/* 定数定義                                                                  */
/*****************************************************************************/

/* 関数戻り値 */
#define D_DTVD_TUNER_OK             0                   /* 成功 */
#define D_DTVD_TUNER_NG             (-1)                /* 失敗 */

/* チャンネル定義 */
#define D_DTVD_TUNER_CHANNEL_MIN    13              /* 開始チャンネル */
#define D_DTVD_TUNER_CHANNEL_MAX    62              /* 最終チャンネル */

/* リクエストコード */
#define D_DTVD_TUNER_IOC_MAGIC          0xE6
#define D_DTVD_TUNER_REQ_PWRON          _IO(D_DTVD_TUNER_IOC_MAGIC, 0)  /* チューナ電源ON要求 */
#define D_DTVD_TUNER_REQ_PWROFF         _IO(D_DTVD_TUNER_IOC_MAGIC, 1)  /* チューナ電源OFF要求 */
#define D_DTVD_TUNER_REQ_INIT           _IO(D_DTVD_TUNER_IOC_MAGIC, 2)  /* チューナ初期化要求 */
#define D_DTVD_TUNER_REQ_END            _IO(D_DTVD_TUNER_IOC_MAGIC, 3)  /* チューナ終了要求 */
#define D_DTVD_TUNER_REQ_TUNE_DTV       _IO(D_DTVD_TUNER_IOC_MAGIC, 4)  /* デジタルテレビ選局要求 */
#define D_DTVD_TUNER_REQ_STOP           _IO(D_DTVD_TUNER_IOC_MAGIC, 5)  /* チューナ停止要求 */
#define D_DTVD_TUNER_REQ_GET_CN         _IO(D_DTVD_TUNER_IOC_MAGIC, 6)  /* 受信レベル取得要求 */
#define D_DTVD_TUNER_REQ_GET_INFO       _IO(D_DTVD_TUNER_IOC_MAGIC, 7)  /* チューナ情報取得要求 */
#define D_DTVD_TUNER_REQ_SET_STYLE      _IO(D_DTVD_TUNER_IOC_MAGIC, 8)  /* スタイル状態設定要求 */
#define D_DTVD_TUNER_REQ_SET_ECO        _IO(D_DTVD_TUNER_IOC_MAGIC, 9)  /* 省電力設定要求 */
#define D_DTVD_TUNER_REQ_INIT_OMT       _IO(D_DTVD_TUNER_IOC_MAGIC, 20) /* チューナ初期化(工程) */
#define D_DTVD_TUNER_REQ_STOP_OMT       _IO(D_DTVD_TUNER_IOC_MAGIC, 21) /* チューナ停止(工程) */
#define D_DTVD_TUNER_REQ_TUNE_DTV_OMT   _IO(D_DTVD_TUNER_IOC_MAGIC, 22) /* 選局(工程) */
#define D_DTVD_TUNER_REQ_BER_START_OMT  _IO(D_DTVD_TUNER_IOC_MAGIC, 23) /* BER値測定開始 */
#define D_DTVD_TUNER_REQ_BER_STOP_OMT   _IO(D_DTVD_TUNER_IOC_MAGIC, 24) /* BER値測定停止 */
#define D_DTVD_TUNER_REQ_BER_GET_OMT    _IO(D_DTVD_TUNER_IOC_MAGIC, 25) /* BER値取得 */
#define D_DTVD_TUNER_REQ_CNR_START_OMT  _IO(D_DTVD_TUNER_IOC_MAGIC, 26) /* C/N値測定開始 */
#define D_DTVD_TUNER_REQ_CNR_STOP_OMT   _IO(D_DTVD_TUNER_IOC_MAGIC, 27) /* C/N値測定停止 */
#define D_DTVD_TUNER_REQ_CNR_GET_OMT    _IO(D_DTVD_TUNER_IOC_MAGIC, 28) /* C/N値取得 */
#define D_DTVD_TUNER_REQ_AGC_START_OMT  _IO(D_DTVD_TUNER_IOC_MAGIC, 29) /* AGC値測定開始 */
#define D_DTVD_TUNER_REQ_AGC_STOP_OMT   _IO(D_DTVD_TUNER_IOC_MAGIC, 30) /* AGC値測定停止 */
#define D_DTVD_TUNER_REQ_AGC_GET_OMT    _IO(D_DTVD_TUNER_IOC_MAGIC, 31) /* AGC値取得 */
#define D_DTVD_TUNER_REQ_ANT_SET_OMT    _IO(D_DTVD_TUNER_IOC_MAGIC, 32) /* アンテナ設定 */

/* 呼び出しモード */
#define D_DTVD_TUNER_CALLMODE_NORMAL    0   /* 通常呼び出し     */
#define D_DTVD_TUNER_CALLMODE_RETRY     1   /* リトライ呼び出し */

/* TS伝送モード */
#define D_DTVD_TUNER_TSMODE_NONE        0   /* 指定なし */
#define D_DTVD_TUNER_TSMODE_MODE2       2   /* TS伝送モード2    */
#define D_DTVD_TUNER_TSMODE_MODE3       3   /* TS伝送モード3    */

/* ガードインターバル */
#define D_DTVD_TUNER_GI_NONE            0   /* 指定なし */
#define D_DTVD_TUNER_GI_1_16            1   /* ガードインターバル 1/16  */
#define D_DTVD_TUNER_GI_1_8             2   /* ガードインターバル 1/8   */
#define D_DTVD_TUNER_GI_1_4             3   /* ガードインターバル 1/4   */

/* 選局種別 */
#define D_DTVD_TUNER_TUNE_KIND_NORMAL   0   /* 通常選局     */
#define D_DTVD_TUNER_TUNE_KIND_SEARCH   1   /* サーチ選局   */
#define D_DTVD_TUNER_TUNE_KIND_SCAN     2   /* チャンネルスキャン   */

/* スタイル状態 */
#define D_DTVD_TUNER_STYLE_NONE         0   /* 状態未設定 */
#define D_DTVD_TUNER_STYLE_NS           1   /* ノーマルスタイル(開状態) */
#define D_DTVD_TUNER_STYLE_CS           2   /* クローズスタイル(閉状態) */
#define D_DTVD_TUNER_STYLE_VS           3   /* 横開き */
#define D_DTVD_TUNER_STYLE_LLS          D_DTVD_TUNER_STYLE_NS   /* 未使用   */
#define D_DTVD_TUNER_STYLE_LRS          D_DTVD_TUNER_STYLE_NS   /* 未使用   */
/*$M #Rev.00.05 INS-S */
#define D_DTVD_TUNER_STYLE_SW           D_DTVD_TUNER_STYLE_NS   /* 未使用   */
/*$M #Rev.00.05 INS-E */

/* 同期確認 */
#define D_DTVD_TUNER_DTVTUNE_SYNC_OFF   0   /* 同期確認なし */
#define D_DTVD_TUNER_DTVTUNE_SYNC_ON    1   /* 同期確認あり */

/* 外部BER測定フラグ */
#define D_DTVD_TUNER_BER_MEASURE_OUT_OFF    0   /* 外部BER測定無効 */
#define D_DTVD_TUNER_BER_MEASURE_OUT_ON     1   /* 外部BER測定有効 */

/* BER測定区間(BERLEN値) */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_1       0               /* パケット数:1     */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_2       1               /* パケット数:2     */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_4       2               /* パケット数:4     */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_8       3               /* パケット数:8     */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_16      4               /* パケット数:16    */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_32      5               /* パケット数:32    */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_64      6               /* パケット数:64    */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_128     7               /* パケット数:128   */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_256     8               /* パケット数:256   */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_512     9               /* パケット数:512   */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_1024    10              /* パケット数:1024  */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_2048    11              /* パケット数:2048  */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_4096    12              /* パケット数:4096  */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_8192    13              /* パケット数:8192  */
/*$M #Rev.00.02 INS-S 1511024 */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_16384   14              /* パケット数:16384 */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_32768   15              /* パケット数:32768 */
#define D_DTVD_TUNER_BER_MEASURE_BERLEN_MAX     D_DTVD_TUNER_BER_MEASURE_BERLEN_32768   /* 最大値 */
/*$M #Rev.00.02 INS-E 1511024 */
/*$M #Rev.00.04 CHG-S */
/*$M #define D_DTVD_TUNER_BER_MEASURE_PACKET( a )    ( 0x0001 << a )*//* パケット数計算式 */
#define D_DTVD_TUNER_BER_MEASURE_PACKET( a )    ( 0x0001 << (a) ) /* パケット数計算式 */
/*$M #Rev.00.04 CHG-E */

/* CN測定キャリア数(CNWIN値) */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_0     0   /* CNWIN値:0 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_1     1   /* CNWIN値:1 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_2     2   /* CNWIN値:2 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_3     3   /* CNWIN値:3 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_4     4   /* CNWIN値:4 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_5     5   /* CNWIN値:5 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_6     6   /* CNWIN値:6 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_7     7   /* CNWIN値:7 */
#define D_DTVD_TUNER_CN_MEASURE_CNWIN_8     D_DTVD_TUNER_CN_MEASURE_CNWIN_7   /* 未使用 */

/* メッセージID */
#define D_DTVD_TUNER_MSGID_TUNE_OK      1   /* 選局成功 */
#define D_DTVD_TUNER_MSGID_TUNE_NG      2   /* 選局失敗 */
#define D_DTVD_TUNER_MSGID_CN_OK        3   /* CN値測定開始通知 */
#define D_DTVD_TUNER_MSGID_SYNC         4   /* RF同期捕捉   */
#define D_DTVD_TUNER_MSGID_ASYNC        5   /* RF同期外れ   */
#define D_DTVD_TUNER_MSGID_DEVERR       6   /* 異常通知     */
/*$M #Rev.01.00 INS-S Laputa */
#define D_DTVD_TUNER_DEVERR_HARD        0   /* HARD ERROR  */
#define D_DTVD_TUNER_DEVERR_TSPOVERFLOW 1   /* TSP受信溢れ */
/*$M #Rev.01.00 INS-E Laputa */

/* チューナDDの機能ブロックID */
#define D_DTVD_TUNER_BLOCK_ID_TUNERDD   6   /* チューナDDの機能ブロックID   */

/* ミドル制御の機能ブロックID */
#define D_DTVD_TUNER_BLOCK_ID_DMM       1   /* ミドル制御の機能ブロックID   */

/* 選局失敗要因 */
#define D_DTVD_TUNER_TUNE_NG_NONE       0   /* 未設定      */
#define D_DTVD_TUNER_TUNE_NG_CAUSE1     1   /* AGC値が閾値未満      */
#define D_DTVD_TUNER_TUNE_NG_CAUSE2     2   /* モードサーチエラー   *//* (未使用) */
#define D_DTVD_TUNER_TUNE_NG_CAUSE3     3   /* 非同期               */
#define D_DTVD_TUNER_TUNE_NG_CAUSE4     4   /* 同期待ちタイムアウト */
#define D_DTVD_TUNER_TUNE_NG_CAUSE5     5   /* 規格違反(TMCC情報取得) */
#define D_DTVD_TUNER_TUNE_NG_CAUSE6     6   /* 規格違反(同期中)       */
#define D_DTVD_TUNER_TUNE_NG_CAUSE7     7   /* フレーム同期後の同期外れ */
#define D_DTVD_TUNER_TUNE_NG_CAUSE8     8   /* サーチ待ち時間タイムアウト */
#define D_DTVD_TUNER_TUNE_NG_CAUSE9     9   /* サーチ失敗 */

/* 取得結果 */
#define D_DTVD_TUNER_GET_RESULT_OK          0       /* 取得成功 */
#define D_DTVD_TUNER_GET_RESULT_NG_NONE     1       /* 取得するデータがない */
#define D_DTVD_TUNER_GET_RESULT_NG_INSUFF   2       /* 時間不十分 *//* (未使用) */

/* 同期状態 */
#define D_DTVD_TUNER_SYNC_STATE_ASYNC   0   /* 非同期 */
#define D_DTVD_TUNER_SYNC_STATE_SYNC    1   /* 同期 */

/* 受信レベル */
#define D_DTVD_TUNER_RX_LEVEL_OUT       0   /* 圏外 */
#define D_DTVD_TUNER_RX_LEVEL_LOW       1   /* 弱   */
#define D_DTVD_TUNER_RX_LEVEL_MDL       2   /* 中   */
#define D_DTVD_TUNER_RX_LEVEL_HIGH      3   /* 強   */

/* 変調方式 */
#define D_DTVD_TUNER_TMCC_MODULATION_DQPSK      0   /* DQPSK */
#define D_DTVD_TUNER_TMCC_MODULATION_QPSK       1   /* QPSK */
#define D_DTVD_TUNER_TMCC_MODULATION_16QAM      2   /* 16QAM */
#define D_DTVD_TUNER_TMCC_MODULATION_64QAM      3   /* 64QAM */
#define D_DTVD_TUNER_TMCC_MODULATION_RSV1       4   /* リザーブ */
#define D_DTVD_TUNER_TMCC_MODULATION_RSV2       5   /* リザーブ */
#define D_DTVD_TUNER_TMCC_MODULATION_RSV3       6   /* リザーブ */
#define D_DTVD_TUNER_TMCC_MODULATION_NONE       7   /* 未使用の階層 */

/* 符号化率 */
#define D_DTVD_TUNER_TMCC_CODERATE_1_2          0   /* 1/2 */
#define D_DTVD_TUNER_TMCC_CODERATE_2_3          1   /* 2/3 */
#define D_DTVD_TUNER_TMCC_CODERATE_3_4          2   /* 3/4 */
#define D_DTVD_TUNER_TMCC_CODERATE_5_6          3   /* 5/6 */
#define D_DTVD_TUNER_TMCC_CODERATE_7_8          4   /* 7/8 */
#define D_DTVD_TUNER_TMCC_CODERATE_RSV1         5   /* リザーブ */
#define D_DTVD_TUNER_TMCC_CODERATE_RSV2         6   /* リザーブ */
#define D_DTVD_TUNER_TMCC_CODERATE_NONE         7   /* 未使用の階層 */

/* インターリーブ長 */
#define D_DTVD_TUNER_TMCC_INTERLEAVE_0          0   /* 0(Mode2)、0(Mode3) */
#define D_DTVD_TUNER_TMCC_INTERLEAVE_1          1   /* 2(Mode2)、1(Mode3) */
#define D_DTVD_TUNER_TMCC_INTERLEAVE_2          2   /* 4(Mode2)、2(Mode3) */
#define D_DTVD_TUNER_TMCC_INTERLEAVE_3          3   /* 8(Mode2)、4(Mode3) */

/* 測定状況 */
#define D_DTVD_TUNER_MEASURE_STATE_OFF          0   /* 測定未実施 */
#define D_DTVD_TUNER_MEASURE_STATE_ON           1   /* 測定中 */
#define D_DTVD_TUNER_MEASURE_STATE_ON_X         2   /* (未使用) */
#define D_DTVD_TUNER_MEASURE_STATE_ON_Y         3   /* (未使用) */

/* TMCCエラーフラグ */
#define D_DTVD_TUNER_TMCC_NOERR         0   /* エラーなし */
#define D_DTVD_TUNER_TMCC_ERROR         1   /* エラーあり */

/* チューナ終了種別 */
#define D_DTVD_TUNER_END_KIND_NORNAL    0   /* 通常終了 */
#define D_DTVD_TUNER_END_KIND_RETRY     1   /* 初期化失敗後の終了 */

/* 省電力モード設定 */
#define D_DTVD_TUNER_ECO_MODE_OFF       0   /* 省電力モードOFF(通常モード) */
#define D_DTVD_TUNER_ECO_MODE_ON        1   /* 省電力モードON */

/* アンテナ種別 */
#define D_DTVD_TUNER_ANT_KIND_WHP       0   /* ホイップアンテナ */
#define D_DTVD_TUNER_ANT_KIND_DPL       1   /* ダイポールアンテナ(未使用) */
#define D_DTVD_TUNER_ANT_KIND_HSJ       2   /* イヤホンアンテナ */


/*****************************************************************************/
/* 構造体定義                                                               */
/*****************************************************************************/

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_CHANNEL_t                                           */
/* ABSTRACT : チャンネル情報構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_CHANNEL_t {
    unsigned char   no;             /* 物理チャンネル番号 */
    unsigned char   seg;            /* 中心セグメント番号 */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_CHANNEL_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_ADJUST_t                                            */
/* ABSTRACT : チャンネル調整値情報構造体                                     */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_ADJUST_t {
    unsigned char   mode;           /* TS伝送モード */
    unsigned char   gi;             /* ガードインターバル比 */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_ADJUST_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MONITOR_RFIC_t                                      */
/* ABSTRACT : RFICモニタ情報構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MONITOR_RFIC_t {
    unsigned char   ant;            /* アンテナ種別 */
    unsigned char   power;          /* 電源状態 */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_MONITOR_RFIC_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MONITOR_TUNE_t                                      */
/* ABSTRACT : 選局モニタ情報構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MONITOR_TUNE_t {
    DTVD_TUNER_CHANNEL_t    ch;         /* チャンネル情報 */
    DTVD_TUNER_ADJUST_t     adj;        /* チャンネル調整値情報 */
    unsigned char           sync_state; /* 同期状態 */
    unsigned char           reserved[3];/* 予約 */
    /* unsigned long          sync_time; */      /* フレーム同期捕捉時間 */
    volatile unsigned long   sync_time; /* フレーム同期捕捉時間(Linux2.6対応)*/
} DTVD_TUNER_MONITOR_TUNE_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_TMCCD_TRANS_PARAM_t                                 */
/* ABSTRACT : 伝送パラメータ情報構造体                                       */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_TMCCD_TRANS_PARAM_t {
    unsigned char   modulation;     /* キャリア変調方式 */
    unsigned char   coderate;       /* 畳み込み符号化率 */
    unsigned char   interleave;     /* インターリーブ長 */
    unsigned char   seg;            /* セグメント数 */
} DTVD_TUNER_TMCCD_TRANS_PARAM_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_TMCC_DATA_t                                         */
/* ABSTRACT : TMCCデータ構造体                                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_TMCC_DATA_t {
    unsigned char                   part;           /* 形式識別フラグ */
    unsigned char                   reserved[3];    /* 予約 */
    DTVD_TUNER_TMCCD_TRANS_PARAM_t  layer_a;        /* A階層伝送パラメータ情報 */
    DTVD_TUNER_TMCCD_TRANS_PARAM_t  layer_b;        /* B階層伝送パラメータ情報 */
} DTVD_TUNER_TMCC_DATA_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_TMCC_INFO_t                                         */
/* ABSTRACT : TMCC情報構造体                                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_TMCC_INFO_t {
    unsigned char           system;     /* システム識別子 */
    unsigned char           cntdwn;     /* 伝送パラメータ切り替え指標(0-15) */
    unsigned char           emgflg;     /* 緊急警報放送用起動フラグ */
    unsigned char           reserved;   /* 予約 */
    DTVD_TUNER_TMCC_DATA_t  curr;       /* カレント情報 */
    DTVD_TUNER_TMCC_DATA_t  next;       /* ネクスト情報 */
} DTVD_TUNER_TMCC_INFO_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MONITOR_TMCC_t                                      */
/* ABSTRACT : TMCCモニタ情報構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MONITOR_TMCC_t {
    unsigned char           error;          /* TMCCエラーなし／あり */
    unsigned char           nonstd;         /* 規格違反 */
    unsigned char           reserved[2];    /* 予約 */
    DTVD_TUNER_TMCC_INFO_t  info;           /* TMCC情報 */
} DTVD_TUNER_MONITOR_TMCC_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MEASURE_AGC_t                                       */
/* ABSTRACT : 測定AGC値構造体                                                */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MEASURE_AGC_t {
    unsigned char   state;      /* 測定状況 */
    unsigned char   value_x;    /* ブランチX(RFICメイン)のAGC値 */
    unsigned char   value_y;    /* ブランチY(RFICサブ)のAGC値 */
    unsigned char   reserved;   /* 予約 */
} DTVD_TUNER_MEASURE_AGC_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MEASURE_VALUE_t                                     */
/* ABSTRACT : 測定値構造体                                                   */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MEASURE_VALUE_t {
    unsigned char   result;         /* 測定結果 */
    unsigned char   up;             /* 上位値 */
    unsigned char   low;            /* 下位値 */
    unsigned char   ext;            /* 拡張領域 *//* 測定値が3byteとなる場合使用する */
} DTVD_TUNER_MEASURE_VALUE_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MEASURE_CNA_t                                       */
/* ABSTRACT : 測定CN値構造体                                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MEASURE_CNA_t {
    unsigned char               state;          /* 測定状況 */
    unsigned char               reserved[3];    /* 予約 */
    DTVD_TUNER_MEASURE_VALUE_t  value_x;        /* ブランチX(RFICメイン)のCN値 */
                                                /* up:CNRDXU low:CNRDXL */
    DTVD_TUNER_MEASURE_VALUE_t  value_y;        /* ブランチY(RFICサブ)のCN値 */
                                                /* up:CNRDYU low::CNRDYL */
    DTVD_TUNER_MEASURE_VALUE_t  value_comp;     /* 合成後の値 */
                                                /* up:CNRDSU low:CNRDSL */
} DTVD_TUNER_MEASURE_CNA_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MEASURE_BER_t                                       */
/* ABSTRACT : 測定BER値構造体                                                */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MEASURE_BER_t {
    unsigned char               state;          /* 測定状況 */
    unsigned char               measure_type;   /* BER値測定種別 */
    unsigned char               reserved[2];    /* 予約 */
    DTVD_TUNER_MEASURE_VALUE_t  value;          /* BER値 */
} DTVD_TUNER_MEASURE_BER_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MONITOR_RX_t                                        */
/* ABSTRACT : 無線部モニタ情報構造体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MONITOR_RX_t {
    DTVD_TUNER_MEASURE_AGC_t agc;   /* 測定AGC値 */
    DTVD_TUNER_MEASURE_CNA_t cna;   /* 測定CNA値 */
    DTVD_TUNER_MEASURE_BER_t ber;   /* 測定BER値 */
} DTVD_TUNER_MONITOR_RX_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MONITOR_INFO_t                                      */
/* ABSTRACT : チューナ情報モニタ構造体                                       */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MONITOR_INFO_t {
    unsigned char               ant_ext_state;  /* イヤホンアンテナ接続状態 */
    unsigned char               diver;          /* ダイバシチ設定・解除状態 */
    unsigned char               reserved[2];    /* 予約 */
    DTVD_TUNER_MONITOR_RFIC_t   rfic_main;      /* RFICメイン情報 */
    DTVD_TUNER_MONITOR_RFIC_t   rfic_sub;       /* RFICサブ情報 */
    DTVD_TUNER_MONITOR_TUNE_t   tune;           /* 選局情報 */
    DTVD_TUNER_MONITOR_TMCC_t   tmcc;           /* TMCC情報 */
    DTVD_TUNER_MONITOR_RX_t     rx;             /* 無線部監視情報 */
} DTVD_TUNER_MONITOR_INFO_t;



/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_PWRON_t                                         */
/* ABSTRACT : 電源ON要求データ構造体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_PWRON_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_PWRON_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_INIT_t                                          */
/* ABSTRACT : 初期化要求データ構造体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_INIT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
/*$M #Rev.01.00 CHG-S Laputa */
/*$M signed int      queue_id;       *//* キューID(ddsync) */

    unsigned char   pipename[256];  /* 名前付きPIPE名 */
/*$M #Rev.01.00 CHG-E Laputa */
} DTVD_TUNER_REQ_INIT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_TUNE_t                                          */
/* ABSTRACT : 選局要求データ構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_TUNE_t {
    unsigned long           seq_id;         /* シーケンスID */
    DTVD_TUNER_CHANNEL_t    ch;             /* チャンネル情報 */
    DTVD_TUNER_ADJUST_t     adj;            /* チャンネル調整値情報 */
    unsigned char           kind;           /* 選局種別 */
    unsigned char           mode;           /* API呼び出しモード */
    unsigned char           reserved[2];    /* 予約 */
} DTVD_TUNER_REQ_TUNE_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_STOP_t                                          */
/* ABSTRACT : 停止要求データ構造体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_STOP_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_STOP_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_END_t                                           */
/* ABSTRACT : 終了要求データ構造体                                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_END_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   kind;           /* 終了種別 */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_REQ_END_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_PWROFF_t                                        */
/* ABSTRACT : 電源OFF要求データ構造体                                        */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_PWROFF_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_PWROFF_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_GET_CN_t                                        */
/* ABSTRACT : 受信レベル取得要求データ構造体                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_GET_CN_t {
    signed int      result;         /* 結果 */
    unsigned char   rx_level;       /* 受信レベル */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_GET_CN_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_GET_INFO_t                                      */
/* ABSTRACT : チューナ情報取得要求データ構造体                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_GET_INFO_t {
    DTVD_TUNER_MONITOR_INFO_t   monitor;    /* チューナモニタ情報 */
} DTVD_TUNER_REQ_GET_INFO_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_SET_STYLE_t                                     */
/* ABSTRACT : スタイル状態設定要求データ構造体                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_SET_STYLE_t {
    unsigned int    style;      /* スタイル */
} DTVD_TUNER_REQ_SET_STYLE_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_SET_ECO_t                                       */
/* ABSTRACT : 省電力モード設定要求データ構造体                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_SET_ECO_t {
   unsigned char       eco_mode;       /* 省電力モードON/OFF */
   unsigned char       reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_SET_ECO_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_INIT_OMT_t                                      */
/* ABSTRACT : 工程コマンド チューナ初期化要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_INIT_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_INIT_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_STOP_OMT_t                                      */
/* ABSTRACT : 工程コマンド チューナ停止要求データ構造体                      */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_STOP_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_STOP_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_TUNE_DTV_OMT_t                                  */
/* ABSTRACT : 工程コマンド DTV選局要求データ構造体                           */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_TUNE_DTV_OMT_t {
    unsigned char       mode;           /* API呼び出しモード */
    unsigned char       ch_no;          /* 物理チャンネル番号 */
    unsigned char       sync;           /* 同期確認あり/なし */
    unsigned char       reserved[2];    /* 予約 */
    signed int          result;         /* 選局結果 */
    DTVD_TUNER_ADJUST_t adj;            /* 選局結果チャンネル調整値情報 */
} DTVD_TUNER_REQ_TUNE_DTV_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_BER_START_OMT_t                                 */
/* ABSTRACT : 工程コマンド  BER値測定開始要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_BER_START_OMT_t {
    unsigned char   mode;       /* API呼び出しモード */
    unsigned char   out;        /* 外部BER測定フラグ(有効/無効) */
    unsigned short  period;     /* 測定するパケット数（BER測定区間） */
} DTVD_TUNER_REQ_BER_START_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_BER_STOP_OMT_t                                  */
/* ABSTRACT : 工程コマンド  BER値測定停止要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_BER_STOP_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_BER_STOP_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_BER_GET_OMT_t                                   */
/* ABSTRACT : 工程コマンド  BER値取得要求データ構造体                        */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_BER_GET_OMT_t {
    unsigned char               mode;           /* API呼び出しモード */
    unsigned char               reserved[3];    /* 予約 */
    DTVD_TUNER_MEASURE_VALUE_t  value;          /* BER値 */
} DTVD_TUNER_REQ_BER_GET_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_CNR_START_OMT_t                                 */
/* ABSTRACT : 工程コマンド  C/N値測定開始要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_CNR_START_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   carrier;        /* 測定するシンボル数(symcountの値) */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_REQ_CNR_START_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_CNR_STOP_OMT_t                                  */
/* ABSTRACT : 工程コマンド  C/N値測定停止要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_CNR_STOP_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_CNR_STOP_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_CNR_GET_OMT_t                                   */
/* ABSTRACT : 工程コマンド  C/N値取得要求データ構造体                        */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_CNR_GET_OMT_t {
    unsigned char               mode;           /* API呼び出しモード */
    unsigned char               reserved[3];    /* 予約 */
    DTVD_TUNER_MEASURE_VALUE_t  value_x;        /* ブランチXのCN値 */
    DTVD_TUNER_MEASURE_VALUE_t  value_y;        /* ブランチYのCN値 */
    DTVD_TUNER_MEASURE_VALUE_t  value_comp;     /* 合成後の値 */
} DTVD_TUNER_REQ_CNR_GET_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_AGC_START_OMT_t                                 */
/* ABSTRACT : 工程コマンド  AGC値測定開始要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_AGC_START_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_AGC_START_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_AGC_STOP_OMT_t                                  */
/* ABSTRACT : 工程コマンド  AGC値測定停止要求データ構造体                    */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_AGC_STOP_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   reserved[3];    /* 予約 */
} DTVD_TUNER_REQ_AGC_STOP_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_AGC_GET_OMT_t                                   */
/* ABSTRACT : 工程コマンド  AGC値取得要求データ構造体                        */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_REQ_AGC_GET_OMT_t {
    unsigned char   mode;       /* API呼び出しモード */
    unsigned char   value_x;    /* ブランチXのAGC値 */
    unsigned char   value_y;    /* ブランチYのAGC値 */
    unsigned char   reserved;   /* 予約 */
} DTVD_TUNER_REQ_AGC_GET_OMT_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_REQ_ANT_SET_OMT_t                                   */
/* ABSTRACT : 工程コマンド  アンテナ設定要求データ構造体                     */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct DTVD_TUNER_REQ_ANT_SET_OMT_t {
    unsigned char   mode;           /* API呼び出しモード */
    unsigned char   ant;            /* アンテナ種別 */
    unsigned char   reserved[2];    /* 予約 */
} DTVD_TUNER_REQ_ANT_SET_OMT_t;

/*****************************************************************************/
/* ミドル制御送信メッセージ情報構造体                                        */
/*****************************************************************************/
/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_TUNE_OK_t                                   */
/* ABSTRACT : 選局成功メッセージデータ構造体                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_TUNE_OK_t {
    unsigned long           seq_id;         /* シーケンスID */
    DTVD_TUNER_ADJUST_t     adj;            /* チャンネル調整値情報 */
    unsigned char           agc;            /* AGC値 */
    unsigned char           reserved[3];    /* 予約 */
} DTVD_TUNER_MSGDATA_TUNE_OK_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_TUNE_NG_t                                   */
/* ABSTRACT : 選局失敗メッセージデータ構造体                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_TUNE_NG_t {
    unsigned long           seq_id;         /* シーケンスID */
    unsigned char           cause;          /* 失敗要因 */
    unsigned char           reserved[3];    /* 予約 */
} DTVD_TUNER_MSGDATA_TUNE_NG_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_SYNC_t                                      */
/* ABSTRACT : RF同期捕捉メッセージデータ構造体                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_SYNC_t {
    DTVD_TUNER_ADJUST_t     adj;            /* チャンネル調整値情報 */
} DTVD_TUNER_MSGDATA_SYNC_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_ASYNC_t                                     */
/* ABSTRACT : RF同期外れメッセージデータ構造体                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_ASYNC_t {
    unsigned char           reserved[4];    /* 予約 */
} DTVD_TUNER_MSGDATA_ASYNC_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_CN_OK_t                                     */
/* ABSTRACT : CN値測定開始メッセージデータ構造体                             */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_CN_OK_t {
    unsigned char           rx_level;       /* 受信レベル */
    unsigned char           reserved[3];    /* 予約 */
} DTVD_TUNER_MSGDATA_CN_OK_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSGDATA_DEVERR_t                                    */
/* ABSTRACT : 異常通知メッセージデータ構造体                                 */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSGDATA_DEVERR_t {
    unsigned char           error_type;     /* ERROR TYPE */
    unsigned char           reserved[3];    /* 予約 */
} DTVD_TUNER_MSGDATA_DEVERR_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSG_HEADER_t                                        */
/* ABSTRACT : メッセージヘッダ構造体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSG_HEADER_t {
    unsigned short  msg_id;         /* メッセージID */
    unsigned char   receiver_id;    /* 受信側ブロックID */
    unsigned char   sender_id;      /* 送信側ブロックID */
} DTVD_TUNER_MSG_HEADER_t;

/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSG_DATA_u                                          */
/* ABSTRACT : メッセージデータ共用体                                         */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
    typedef union DTVD_TUNER_MSG_DATA_u {
    DTVD_TUNER_MSGDATA_TUNE_OK_t    tune_ok;    /* 選局成功メッセージデータ */
    DTVD_TUNER_MSGDATA_TUNE_NG_t    tune_ng;    /* 選局失敗メッセージデータ */
    DTVD_TUNER_MSGDATA_SYNC_t       sync;       /* RF同期捕捉メッセージデータ */
    DTVD_TUNER_MSGDATA_ASYNC_t      async;      /* RF同期外れメッセージデータ */
    DTVD_TUNER_MSGDATA_CN_OK_t      cn_ok;      /* CN値測定開始メッセージデータ */
    DTVD_TUNER_MSGDATA_DEVERR_t     deverr;     /* 異常通知メッセージデータ */
    unsigned char                   dummy[60];  /* ダミー */
} DTVD_TUNER_MSG_DATA_u;


/*---------------------------------------------------------------------------*/
/* TAG      : DTVD_TUNER_MSG_t                                               */
/* ABSTRACT : メッセージ構造体                                               */
/* NOTE     :                                                                */
/*---------------------------------------------------------------------------*/
typedef struct _DTVD_TUNER_MSG_t {
    DTVD_TUNER_MSG_HEADER_t     header;     /* メッセージヘッダ */
    DTVD_TUNER_MSG_DATA_u       data;       /* メッセージデータ */
} DTVD_TUNER_MSG_t;


/*****************************************************************************/
/* ＡＰＩ関数プロトタイプ宣言                                                */
/*****************************************************************************/

#endif /* _DTVTUNER_H_ */
/*****************************************************************************/
/*   Copyright(C) 2007 Panasonic Mobile Communications Co.,Ltd.              */
/*****************************************************************************/
