.class Lcom/scottmain/android/searchlight/PreviewSurface;
.super Landroid/view/SurfaceView;
.source "PreviewSurface.java"

# interfaces
.implements Landroid/view/SurfaceHolder$Callback;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/scottmain/android/searchlight/PreviewSurface$Callback;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String; = "PreviewSurface"

.field private static cameraInfoSupported:Z


# instance fields
.field hasCamera:Z

.field hasSurface:Z

.field isViewfinder:Z

.field mActivity:Landroid/app/Activity;

.field mCallback:Lcom/scottmain/android/searchlight/PreviewSurface$Callback;

.field mCamera:Landroid/hardware/Camera;

.field mContext:Landroid/content/Context;

.field mHeight:I

.field mHolder:Landroid/view/SurfaceHolder;

.field mParameters:Landroid/hardware/Camera$Parameters;

.field mWidth:I


# direct methods
.method static constructor <clinit>()V
    .locals 2

    .line 47
    const/4 v0, 0x0

    .line 47
    sput-boolean v0, Lcom/scottmain/android/searchlight/PreviewSurface;->cameraInfoSupported:Z

    .line 55
    :try_start_0
    invoke-static {}, Lcom/scottmain/android/searchlight/PreviewSurface;->checkCameraInfoAvailable()V
    :try_end_0
    .catch Ljava/lang/NoClassDefFoundError; {:try_start_0 .. :try_end_0} :catch_0

    .line 59
    return-void

    .line 56
    :catch_0
    move-exception v1

    .line 57
    .local v1, "$r0":Ljava/lang/NoClassDefFoundError;, ""
    const/4 v0, 0x0

    .line 57
    sput-boolean v0, Lcom/scottmain/android/searchlight/PreviewSurface;->cameraInfoSupported:Z

    return-void
    .end local v1    # "$r0":Ljava/lang/NoClassDefFoundError;, ""
.end method

.method constructor <init>(Landroid/content/Context;)V
    .locals 1
    .param p1, "context"    # Landroid/content/Context;

    .line 62
    invoke-direct {p0, p1}, Landroid/view/SurfaceView;-><init>(Landroid/content/Context;)V

    .line 43
    const/4 v0, 0x0

    .line 43
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .line 44
    const/4 v0, 0x0

    .line 44
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasSurface:Z

    .line 45
    const/4 v0, 0x0

    .line 45
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->isViewfinder:Z

    .line 63
    iput-object p1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mContext:Landroid/content/Context;

    .line 64
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->initHolder()V

    .line 65
    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 1
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "attrs"    # Landroid/util/AttributeSet;

    .line 73
    invoke-direct {p0, p1, p2}, Landroid/view/SurfaceView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 43
    const/4 v0, 0x0

    .line 43
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .line 44
    const/4 v0, 0x0

    .line 44
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasSurface:Z

    .line 45
    const/4 v0, 0x0

    .line 45
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->isViewfinder:Z

    .line 74
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->initHolder()V

    .line 75
    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V
    .locals 1
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "attrs"    # Landroid/util/AttributeSet;
    .param p3, "defStyle"    # I

    .line 68
    invoke-direct {p0, p1, p2, p3}, Landroid/view/SurfaceView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    .line 43
    const/4 v0, 0x0

    .line 43
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .line 44
    const/4 v0, 0x0

    .line 44
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasSurface:Z

    .line 45
    const/4 v0, 0x0

    .line 45
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->isViewfinder:Z

    .line 69
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->initHolder()V

    .line 70
    return-void
.end method

.method private static checkCameraInfoAvailable()V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/NoClassDefFoundError;
        }
    .end annotation

    const/4 v0, 0x1

    sput-boolean v0, Lcom/scottmain/android/searchlight/PreviewSurface;->cameraInfoSupported:Z

    .line 51
    return-void
.end method

.method private getOptimalPreviewSize(Ljava/util/List;II)Landroid/hardware/Camera$Size;
    .locals 21
    .param p2, "w"    # I
    .param p3, "h"    # I
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List",
            "<",
            "Landroid/hardware/Camera$Size;",
            ">;II)",
            "Landroid/hardware/Camera$Size;"
        }
    .end annotation

    move/from16 v0, p2

    .local v2, "$d1":D, ""
    int-to-double v2, v0

    move/from16 v0, p3

    .local v4, "$d2":D, ""
    int-to-double v4, v0

    div-double/2addr v2, v4

    if-nez p1, :cond_0

    .line 256
    const/4 v6, 0x0

    .line 256
    return-object v6

    .line 230
    :cond_0
    const/4 v7, 0x0

    .line 231
    .local v7, "$r2":Landroid/hardware/Camera$Size;, ""
    const-wide v4, 0x7fefffffffffffffL    # Double.MAX_VALUE

    .line 236
    move-object/from16 v0, p1

    .line 236
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v8

    .line 236
    .local v8, "$r3":Ljava/util/Iterator;, ""
    :cond_1
    :goto_0
    invoke-interface {v8}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    .local v9, "$z0":Z, ""
    if-eqz v9, :cond_2

    .line 236
    invoke-interface {v8}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v10

    .local v10, "$r4":Ljava/lang/Object;, ""
    move-object v12, v10

    check-cast v12, Landroid/hardware/Camera$Size;

    move-object v11, v12

    .line 237
    .local v11, "$r5":Landroid/hardware/Camera$Size;, ""
    iget v0, v11, Landroid/hardware/Camera$Size;->width:I

    .line 237
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    int-to-double v13, v0

    .local v13, "$d0":D, ""
    iget v0, v11, Landroid/hardware/Camera$Size;->height:I

    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    int-to-double v0, v0

    .local v0, "$d3":D, ""
    move-wide/from16 v15, v0

    .end local v0    # "$d3":D, ""
    .local v15, "$d3":D, ""
    div-double/2addr v13, v0

    .line 238
    sub-double/2addr v13, v2

    .line 238
    invoke-static {v13, v14}, Ljava/lang/Math;->abs(D)D

    move-result-wide v13

    const-wide v18, 0x3fa999999999999aL    # 0.05

    cmpl-double v17, v13, v18

    .local v17, "$b2":B, ""
    if-gtz v17, :cond_1

    .line 239
    iget v0, v11, Landroid/hardware/Camera$Size;->height:I

    .line 239
    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v1, p3

    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    sub-int/2addr v0, v1

    move/from16 p2, v0

    .line 239
    invoke-static {v0}, Ljava/lang/Math;->abs(I)I

    move-result p2

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v0, p2

    int-to-double v13, v0

    cmpg-double v17, v13, v4

    if-gez v17, :cond_1

    .line 240
    move-object v7, v11

    .line 241
    iget v0, v11, Landroid/hardware/Camera$Size;->height:I

    .line 241
    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v1, p3

    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    sub-int/2addr v0, v1

    move/from16 p2, v0

    .line 241
    invoke-static {v0}, Ljava/lang/Math;->abs(I)I

    move-result p2

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v0, p2

    int-to-double v4, v0

    goto :goto_0

    :cond_2
    if-nez v7, :cond_4

    .line 247
    const-wide v2, 0x7fefffffffffffffL    # Double.MAX_VALUE

    .line 248
    move-object/from16 v0, p1

    .line 248
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v8

    .line 248
    :cond_3
    :goto_1
    invoke-interface {v8}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-eqz v9, :cond_4

    .line 248
    invoke-interface {v8}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v10

    move-object/from16 v20, v10

    check-cast v20, Landroid/hardware/Camera$Size;

    move-object/from16 v11, v20

    .line 249
    iget v0, v11, Landroid/hardware/Camera$Size;->height:I

    .line 249
    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v1, p3

    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    sub-int/2addr v0, v1

    move/from16 p2, v0

    .line 249
    invoke-static {v0}, Ljava/lang/Math;->abs(I)I

    move-result p2

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v0, p2

    int-to-double v4, v0

    cmpg-double v17, v4, v2

    if-gez v17, :cond_3

    .line 250
    move-object v7, v11

    .line 251
    iget v0, v11, Landroid/hardware/Camera$Size;->height:I

    .line 251
    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    move/from16 p2, v0

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v1, p3

    .end local p2    # "$i0":I, ""
    .local v0, "$i0":I, ""
    sub-int/2addr v0, v1

    move/from16 p2, v0

    .line 251
    invoke-static {v0}, Ljava/lang/Math;->abs(I)I

    move-result p2

    .end local v0    # "$i0":I, ""
    .local p2, "$i0":I, ""
    move/from16 v0, p2

    int-to-double v2, v0

    goto :goto_1

    :cond_4
    return-object v7
    .end local v15    # "$d3":D, ""
    .end local v4    # "$d2":D, ""
    .end local p2    # "$i0":I, ""
    .end local v2    # "$d1":D, ""
    .end local v7    # "$r2":Landroid/hardware/Camera$Size;, ""
    .end local v11    # "$r5":Landroid/hardware/Camera$Size;, ""
    .end local v8    # "$r3":Ljava/util/Iterator;, ""
    .end local v13    # "$d0":D, ""
    .end local v9    # "$z0":Z, ""
    .end local v17    # "$b2":B, ""
    .end local v10    # "$r4":Ljava/lang/Object;, ""
.end method

.method private initHolder()V
    .locals 2

    .line 80
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->getHolder()Landroid/view/SurfaceHolder;

    move-result-object v0

    .local v0, "$r1":Landroid/view/SurfaceHolder;, ""
    iput-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHolder:Landroid/view/SurfaceHolder;

    .line 81
    iget-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHolder:Landroid/view/SurfaceHolder;

    .line 81
    invoke-interface {v0, p0}, Landroid/view/SurfaceHolder;->addCallback(Landroid/view/SurfaceHolder$Callback;)V

    .line 82
    iget-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHolder:Landroid/view/SurfaceHolder;

    .line 82
    const/4 v1, 0x3

    .line 82
    invoke-interface {v0, v1}, Landroid/view/SurfaceHolder;->setType(I)V

    .line 83
    return-void
    .end local v0    # "$r1":Landroid/view/SurfaceHolder;, ""
.end method

.method private setCameraDisplayOrientation()V
    .locals 9

    .line 131
    sget-boolean v0, Lcom/scottmain/android/searchlight/PreviewSurface;->cameraInfoSupported:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 132
    new-instance v1, Landroid/hardware/Camera$CameraInfo;

    .line 132
    .local v1, "$r1":Landroid/hardware/Camera$CameraInfo;, ""
    invoke-direct {v1}, Landroid/hardware/Camera$CameraInfo;-><init>()V

    .line 134
    const/4 v2, 0x0

    .line 134
    invoke-static {v2, v1}, Landroid/hardware/Camera;->getCameraInfo(ILandroid/hardware/Camera$CameraInfo;)V

    .line 135
    iget-object v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mActivity:Landroid/app/Activity;

    .line 135
    .local v3, "$r2":Landroid/app/Activity;, ""
    invoke-virtual {v3}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object v4

    .line 135
    .local v4, "$r3":Landroid/view/WindowManager;, ""
    invoke-interface {v4}, Landroid/view/WindowManager;->getDefaultDisplay()Landroid/view/Display;

    move-result-object v5

    .line 135
    .local v5, "$r4":Landroid/view/Display;, ""
    invoke-virtual {v5}, Landroid/view/Display;->getRotation()I

    move-result v6

    .line 137
    .local v6, "$i0":I, ""
    const/4 v7, 0x0

    .local v7, "$s1":S, ""
    sparse-switch v6, :sswitch_data_0

    goto :goto_0

    .line 145
    :goto_0
    iget v6, v1, Landroid/hardware/Camera$CameraInfo;->orientation:I

    sub-int/2addr v6, v7

    add-int/lit16 v6, v6, 0x168

    rem-int/lit16 v6, v6, 0x168

    .line 146
    iget-object v8, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 146
    .local v8, "$r5":Landroid/hardware/Camera;, ""
    invoke-virtual {v8, v6}, Landroid/hardware/Camera;->setDisplayOrientation(I)V

    .line 150
    return-void

    .line 139
    :sswitch_0
    const/4 v7, 0x0

    goto :goto_0

    .line 140
    :sswitch_1
    const/16 v7, 0x5a

    goto :goto_0

    .line 141
    :sswitch_2
    const/16 v7, 0xb4

    goto :goto_0

    .line 142
    :sswitch_3
    const/16 v7, 0x10e

    goto :goto_0

    .line 148
    :cond_0
    iget-object v8, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 148
    const/16 v2, 0x5a

    .line 148
    invoke-virtual {v8, v2}, Landroid/hardware/Camera;->setDisplayOrientation(I)V

    return-void

    :sswitch_data_0
    .sparse-switch
        0x0 -> :sswitch_0
        0x1 -> :sswitch_1
        0x2 -> :sswitch_2
        0x3 -> :sswitch_3
    .end sparse-switch
    .end local v4    # "$r3":Landroid/view/WindowManager;, ""
    .end local v8    # "$r5":Landroid/hardware/Camera;, ""
    .end local v3    # "$r2":Landroid/app/Activity;, ""
    .end local v0    # "$z0":Z, ""
    .end local v6    # "$i0":I, ""
    .end local v7    # "$s1":S, ""
    .end local v1    # "$r1":Landroid/hardware/Camera$CameraInfo;, ""
    .end local v5    # "$r4":Landroid/view/Display;, ""
.end method

.method private setParameters()V
    .locals 7

    .line 116
    iget-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 116
    .local v0, "$r1":Landroid/hardware/Camera;, ""
    invoke-virtual {v0}, Landroid/hardware/Camera;->getParameters()Landroid/hardware/Camera$Parameters;

    move-result-object v1

    .local v1, "$r2":Landroid/hardware/Camera$Parameters;, ""
    iput-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 118
    iget-boolean v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->isViewfinder:Z

    .local v2, "$z0":Z, ""
    if-eqz v2, :cond_0

    .line 119
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 119
    invoke-virtual {v1}, Landroid/hardware/Camera$Parameters;->getSupportedPreviewSizes()Ljava/util/List;

    move-result-object v3

    .line 122
    .local v3, "$r3":Ljava/util/List;, ""
    iget v4, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHeight:I

    .local v4, "$i0":I, ""
    iget v5, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mWidth:I

    .line 122
    .local v5, "$i1":I, ""
    invoke-direct {p0, v3, v4, v5}, Lcom/scottmain/android/searchlight/PreviewSurface;->getOptimalPreviewSize(Ljava/util/List;II)Landroid/hardware/Camera$Size;

    move-result-object v6

    .line 123
    .local v6, "$r4":Landroid/hardware/Camera$Size;, ""
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    iget v5, v6, Landroid/hardware/Camera$Size;->width:I

    iget v4, v6, Landroid/hardware/Camera$Size;->height:I

    .line 123
    invoke-virtual {v1, v5, v4}, Landroid/hardware/Camera$Parameters;->setPreviewSize(II)V

    .line 124
    iget-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 124
    invoke-virtual {v0, v1}, Landroid/hardware/Camera;->setParameters(Landroid/hardware/Camera$Parameters;)V

    .line 127
    :cond_0
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->setCameraDisplayOrientation()V

    .line 128
    return-void
    .end local v2    # "$z0":Z, ""
    .end local v4    # "$i0":I, ""
    .end local v3    # "$r3":Ljava/util/List;, ""
    .end local v0    # "$r1":Landroid/hardware/Camera;, ""
    .end local v1    # "$r2":Landroid/hardware/Camera$Parameters;, ""
    .end local v6    # "$r4":Landroid/hardware/Camera$Size;, ""
    .end local v5    # "$i1":I, ""
.end method


# virtual methods
.method public hasCamera()Z
    .locals 1

    .line 195
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .local v0, "z0":Z, ""
    return v0
    .end local v0    # "z0":Z, ""
.end method

.method public initCamera()V
    .locals 12

    .line 153
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .local v0, "$z0":Z, ""
    if-nez v0, :cond_0

    .line 155
    :try_start_0
    invoke-static {}, Landroid/hardware/Camera;->open()Landroid/hardware/Camera;

    move-result-object v1

    .local v1, "$r3":Landroid/hardware/Camera;, ""
    iput-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    const/4 v2, 0x1

    iput-boolean v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z
    :try_end_0
    .catch Ljava/lang/RuntimeException; {:try_start_0 .. :try_end_0} :catch_0

    .line 164
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    iget-object v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHolder:Landroid/view/SurfaceHolder;

    .line 164
    .local v3, "$r4":Landroid/view/SurfaceHolder;, ""
    :try_start_1
    invoke-virtual {v1, v3}, Landroid/hardware/Camera;->setPreviewDisplay(Landroid/view/SurfaceHolder;)V
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_1

    .line 173
    return-void

    .line 157
    :catch_0
    move-exception v4

    .local v4, "$r1":Ljava/lang/RuntimeException;, ""
    new-instance v5, Ljava/lang/StringBuilder;

    .line 158
    .local v5, "$r5":Ljava/lang/StringBuilder;, ""
    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    .line 158
    const-string v6, "Could not open Camera"

    .line 158
    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    .line 158
    invoke-virtual {v5, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v5

    .line 158
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    .line 158
    .local v7, "$r6":Ljava/lang/String;, ""
    const-string v6, "PreviewSurface"

    .line 158
    invoke-static {v6, v7}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 159
    const/4 v2, 0x0

    .line 159
    iput-boolean v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .line 160
    iget-object v8, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCallback:Lcom/scottmain/android/searchlight/PreviewSurface$Callback;

    .line 160
    .local v8, "$r7":Lcom/scottmain/android/searchlight/PreviewSurface$Callback;, ""
    invoke-interface {v8}, Lcom/scottmain/android/searchlight/PreviewSurface$Callback;->cameraNotAvailable()V

    return-void

    .line 165
    :catch_1
    move-exception v9

    .line 166
    .local v9, "$r2":Ljava/io/IOException;, ""
    const-string v6, "PreviewSurface"

    .line 166
    const-string v10, "Could not set preview surface"

    .line 166
    invoke-static {v6, v10}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 167
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 167
    invoke-virtual {v1}, Landroid/hardware/Camera;->release()V

    const/4 v11, 0x0

    iput-object v11, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 169
    const/4 v2, 0x0

    .line 169
    iput-boolean v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    :cond_0
    return-void
    .end local v3    # "$r4":Landroid/view/SurfaceHolder;, ""
    .end local v9    # "$r2":Ljava/io/IOException;, ""
    .end local v8    # "$r7":Lcom/scottmain/android/searchlight/PreviewSurface$Callback;, ""
    .end local v1    # "$r3":Landroid/hardware/Camera;, ""
    .end local v5    # "$r5":Ljava/lang/StringBuilder;, ""
    .end local v4    # "$r1":Ljava/lang/RuntimeException;, ""
    .end local v0    # "$z0":Z, ""
    .end local v7    # "$r6":Ljava/lang/String;, ""
.end method

.method public lightOff()V
    .locals 4

    .line 176
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasSurface:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    if-eqz v0, :cond_0

    .line 177
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 177
    .local v1, "$r1":Landroid/hardware/Camera$Parameters;, ""
    const-string v2, "off"

    .line 177
    invoke-virtual {v1, v2}, Landroid/hardware/Camera$Parameters;->setFlashMode(Ljava/lang/String;)V

    .line 178
    iget-object v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .local v3, "$r2":Landroid/hardware/Camera;, ""
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 178
    invoke-virtual {v3, v1}, Landroid/hardware/Camera;->setParameters(Landroid/hardware/Camera$Parameters;)V

    .line 180
    :cond_0
    return-void
    .end local v3    # "$r2":Landroid/hardware/Camera;, ""
    .end local v1    # "$r1":Landroid/hardware/Camera$Parameters;, ""
    .end local v0    # "$z0":Z, ""
.end method

.method public lightOn()V
    .locals 4

    .line 183
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->isShown()Z

    move-result v0

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_1

    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    if-eqz v0, :cond_1

    .line 184
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .local v1, "$r1":Landroid/hardware/Camera$Parameters;, ""
    if-nez v1, :cond_0

    .line 185
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->setParameters()V

    .line 187
    :cond_0
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 187
    const-string v2, "torch"

    .line 187
    invoke-virtual {v1, v2}, Landroid/hardware/Camera$Parameters;->setFlashMode(Ljava/lang/String;)V

    .line 188
    iget-object v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .local v3, "$r2":Landroid/hardware/Camera;, ""
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mParameters:Landroid/hardware/Camera$Parameters;

    .line 188
    invoke-virtual {v3, v1}, Landroid/hardware/Camera;->setParameters(Landroid/hardware/Camera$Parameters;)V

    .line 192
    return-void

    .line 190
    :cond_1
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->initCamera()V

    return-void
    .end local v0    # "$z0":Z, ""
    .end local v3    # "$r2":Landroid/hardware/Camera;, ""
    .end local v1    # "$r1":Landroid/hardware/Camera$Parameters;, ""
.end method

.method public releaseCamera()V
    .locals 4

    .line 208
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 209
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 209
    .local v1, "$r1":Landroid/hardware/Camera;, ""
    invoke-virtual {v1}, Landroid/hardware/Camera;->stopPreview()V

    .line 210
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 210
    invoke-virtual {v1}, Landroid/hardware/Camera;->release()V

    const/4 v2, 0x0

    iput-object v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    const/4 v3, 0x0

    iput-boolean v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .line 214
    :cond_0
    return-void
    .end local v0    # "$z0":Z, ""
    .end local v1    # "$r1":Landroid/hardware/Camera;, ""
.end method

.method public setCallback(Lcom/scottmain/android/searchlight/PreviewSurface$Callback;)V
    .locals 2
    .param p1, "c"    # Lcom/scottmain/android/searchlight/PreviewSurface$Callback;

    .line 199
    iput-object p1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCallback:Lcom/scottmain/android/searchlight/PreviewSurface$Callback;

    .line 200
    move-object v1, p1

    .line 200
    check-cast v1, Landroid/app/Activity;

    .line 200
    move-object v0, v1

    .local v0, "$r2":Landroid/app/Activity;, ""
    iput-object v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mActivity:Landroid/app/Activity;

    .line 201
    return-void
    .end local v0    # "$r2":Landroid/app/Activity;, ""
.end method

.method public setIsViewfinder()V
    .locals 1

    const/4 v0, 0x1

    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->isViewfinder:Z

    .line 205
    return-void
.end method

.method public startPreview()V
    .locals 3

    .line 217
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 218
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->setParameters()V

    .line 219
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 219
    .local v1, "$r1":Landroid/hardware/Camera;, ""
    const/16 v2, 0x5a

    .line 219
    invoke-virtual {v1, v2}, Landroid/hardware/Camera;->setDisplayOrientation(I)V

    .line 220
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 220
    invoke-virtual {v1}, Landroid/hardware/Camera;->startPreview()V

    .line 222
    :cond_0
    return-void
    .end local v0    # "$z0":Z, ""
    .end local v1    # "$r1":Landroid/hardware/Camera;, ""
.end method

.method public surfaceChanged(Landroid/view/SurfaceHolder;III)V
    .locals 4
    .param p1, "holder"    # Landroid/view/SurfaceHolder;
    .param p2, "format"    # I
    .param p3, "w"    # I
    .param p4, "h"    # I

    .line 104
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasCamera:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 105
    iput p3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mWidth:I

    .line 106
    iput p4, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHeight:I

    .line 107
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->setParameters()V

    .line 108
    iget-object v1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCamera:Landroid/hardware/Camera;

    .line 108
    .local v1, "$r2":Landroid/hardware/Camera;, ""
    invoke-virtual {v1}, Landroid/hardware/Camera;->startPreview()V

    .line 109
    iget-object v2, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mCallback:Lcom/scottmain/android/searchlight/PreviewSurface$Callback;

    .line 109
    .local v2, "$r3":Lcom/scottmain/android/searchlight/PreviewSurface$Callback;, ""
    invoke-interface {v2}, Lcom/scottmain/android/searchlight/PreviewSurface$Callback;->cameraReady()V

    const/4 v3, 0x1

    iput-boolean v3, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->hasSurface:Z

    .line 113
    :cond_0
    return-void
    .end local v1    # "$r2":Landroid/hardware/Camera;, ""
    .end local v2    # "$r3":Lcom/scottmain/android/searchlight/PreviewSurface$Callback;, ""
    .end local v0    # "$z0":Z, ""
.end method

.method public surfaceCreated(Landroid/view/SurfaceHolder;)V
    .locals 0
    .param p1, "holder"    # Landroid/view/SurfaceHolder;

    .line 88
    iput-object p1, p0, Lcom/scottmain/android/searchlight/PreviewSurface;->mHolder:Landroid/view/SurfaceHolder;

    .line 89
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->initCamera()V

    .line 91
    return-void
.end method

.method public surfaceDestroyed(Landroid/view/SurfaceHolder;)V
    .locals 0
    .param p1, "holder"    # Landroid/view/SurfaceHolder;

    .line 97
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/PreviewSurface;->releaseCamera()V

    .line 99
    return-void
.end method
