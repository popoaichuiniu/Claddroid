.class public Lcom/scottmain/android/searchlight/SearchLight;
.super Landroid/app/Activity;
.source "SearchLight.java"

# interfaces
.implements Lcom/scottmain/android/searchlight/PreviewSurface$Callback;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/scottmain/android/searchlight/SearchLight$1;
    }
.end annotation


# static fields
.field private static final MODE_TYPE:Ljava/lang/String; = "mode_type"


# instance fields
.field bulb:Landroid/widget/ImageButton;

.field mCurrentMode:I

.field mDrawable:Landroid/graphics/drawable/TransitionDrawable;

.field mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

.field on:Z

.field paused:Z

.field skipAnimate:Z


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 38
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    .line 45
    const/4 v0, 0x0

    .line 45
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .line 46
    const/4 v0, 0x0

    .line 46
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .line 47
    const/4 v0, 0x0

    .line 47
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->skipAnimate:Z

    const v0, 0x7f050001

    iput v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->mCurrentMode:I

    return-void
.end method

.method private turnOff()V
    .locals 4

    .line 116
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    const/4 v1, 0x0

    iput-boolean v1, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .line 118
    iget-object v2, p0, Lcom/scottmain/android/searchlight/SearchLight;->mDrawable:Landroid/graphics/drawable/TransitionDrawable;

    .line 118
    .local v2, "$r1":Landroid/graphics/drawable/TransitionDrawable;, ""
    const/16 v1, 0x12c

    .line 118
    invoke-virtual {v2, v1}, Landroid/graphics/drawable/TransitionDrawable;->reverseTransition(I)V

    .line 119
    iget-object v3, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 119
    .local v3, "$r2":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v3}, Lcom/scottmain/android/searchlight/PreviewSurface;->lightOff()V

    .line 121
    :cond_0
    return-void
    .end local v0    # "$z0":Z, ""
    .end local v2    # "$r1":Landroid/graphics/drawable/TransitionDrawable;, ""
    .end local v3    # "$r2":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
.end method

.method private turnOn()V
    .locals 4

    .line 108
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .local v0, "$z0":Z, ""
    if-nez v0, :cond_0

    const/4 v1, 0x1

    iput-boolean v1, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .line 110
    iget-object v2, p0, Lcom/scottmain/android/searchlight/SearchLight;->mDrawable:Landroid/graphics/drawable/TransitionDrawable;

    .line 110
    .local v2, "$r1":Landroid/graphics/drawable/TransitionDrawable;, ""
    const/16 v1, 0xc8

    .line 110
    invoke-virtual {v2, v1}, Landroid/graphics/drawable/TransitionDrawable;->startTransition(I)V

    .line 111
    iget-object v3, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 111
    .local v3, "$r2":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v3}, Lcom/scottmain/android/searchlight/PreviewSurface;->lightOn()V

    .line 113
    :cond_0
    return-void
    .end local v3    # "$r2":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    .end local v0    # "$z0":Z, ""
    .end local v2    # "$r1":Landroid/graphics/drawable/TransitionDrawable;, ""
.end method


# virtual methods
.method public cameraNotAvailable()V
    .locals 1

    .line 261
    const v0, 0x7f050000

    .line 261
    invoke-virtual {p0, v0}, Lcom/scottmain/android/searchlight/SearchLight;->showDialog(I)V

    .line 262
    return-void
.end method

.method public cameraReady()V
    .locals 0

    .line 257
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/SearchLight;->turnOn()V

    .line 258
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 18
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .line 53
    move-object/from16 v0, p0

    .line 53
    move-object/from16 v1, p1

    .line 53
    invoke-super {v0, v1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 57
    move-object/from16 v0, p0

    .line 57
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/SearchLight;->getIntent()Landroid/content/Intent;

    move-result-object v2

    .line 57
    .local v2, "$r2":Landroid/content/Intent;, ""
    const-string v4, "mode_type"

    .line 57
    const/4 v5, 0x0

    .line 57
    invoke-virtual {v2, v4, v5}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v3

    .local v3, "$i0":I, ""
    move v6, v3

    .local v6, "$i1":I, ""
    if-nez v3, :cond_0

    .line 60
    const/4 v5, 0x0

    .line 60
    move-object/from16 v0, p0

    .line 60
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->getPreferences(I)Landroid/content/SharedPreferences;

    move-result-object v7

    .line 61
    .local v7, "$r3":Landroid/content/SharedPreferences;, ""
    const-string v4, "mode_type"

    .line 61
    const/4 v5, 0x0

    .line 61
    invoke-interface {v7, v4, v5}, Landroid/content/SharedPreferences;->getInt(Ljava/lang/String;I)I

    move-result v3

    move v6, v3

    .line 64
    move-object/from16 v0, p0

    .line 64
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/SearchLight;->getIntent()Landroid/content/Intent;

    move-result-object v2

    .line 65
    const-string v4, "mode_type"

    .line 65
    invoke-virtual {v2, v4, v3}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 66
    move-object/from16 v0, p0

    .line 66
    invoke-virtual {v0, v2}, Lcom/scottmain/android/searchlight/SearchLight;->setIntent(Landroid/content/Intent;)V

    :cond_0
    sparse-switch v6, :sswitch_data_0

    goto :goto_0

    .line 83
    :goto_0
    const v5, 0x7f030001

    .line 83
    move-object/from16 v0, p0

    .line 83
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->setContentView(I)V

    const v5, 0x7f050001

    move-object/from16 v0, p0

    iput v5, v0, Lcom/scottmain/android/searchlight/SearchLight;->mCurrentMode:I

    .line 88
    :goto_1
    const v5, 0x7f050004

    .line 88
    move-object/from16 v0, p0

    .line 88
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->findViewById(I)Landroid/view/View;

    move-result-object v8

    .local v8, "$r4":Landroid/view/View;, ""
    move-object v10, v8

    check-cast v10, Lcom/scottmain/android/searchlight/PreviewSurface;

    move-object v9, v10

    .local v9, "$r5":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    move-object/from16 v0, p0

    iput-object v9, v0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 89
    move-object/from16 v0, p0

    .line 89
    iget-object v9, v0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 89
    move-object/from16 v0, p0

    .line 89
    invoke-virtual {v9, v0}, Lcom/scottmain/android/searchlight/PreviewSurface;->setCallback(Lcom/scottmain/android/searchlight/PreviewSurface$Callback;)V

    const v5, 0x7f050002

    if-ne v6, v5, :cond_1

    .line 91
    move-object/from16 v0, p0

    .line 91
    iget-object v9, v0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 91
    invoke-virtual {v9}, Lcom/scottmain/android/searchlight/PreviewSurface;->setIsViewfinder()V

    .line 94
    :cond_1
    const v5, 0x7f050006

    .line 94
    move-object/from16 v0, p0

    .line 94
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->findViewById(I)Landroid/view/View;

    move-result-object v8

    move-object v12, v8

    check-cast v12, Landroid/widget/ImageButton;

    move-object v11, v12

    .local v11, "$r6":Landroid/widget/ImageButton;, ""
    move-object/from16 v0, p0

    iput-object v11, v0, Lcom/scottmain/android/searchlight/SearchLight;->bulb:Landroid/widget/ImageButton;

    .line 95
    move-object/from16 v0, p0

    .line 95
    iget-object v11, v0, Lcom/scottmain/android/searchlight/SearchLight;->bulb:Landroid/widget/ImageButton;

    .line 95
    invoke-virtual {v11}, Landroid/widget/ImageButton;->getDrawable()Landroid/graphics/drawable/Drawable;

    move-result-object v13

    .local v13, "$r7":Landroid/graphics/drawable/Drawable;, ""
    move-object v15, v13

    check-cast v15, Landroid/graphics/drawable/TransitionDrawable;

    move-object v14, v15

    .local v14, "$r8":Landroid/graphics/drawable/TransitionDrawable;, ""
    move-object/from16 v0, p0

    iput-object v14, v0, Lcom/scottmain/android/searchlight/SearchLight;->mDrawable:Landroid/graphics/drawable/TransitionDrawable;

    .line 96
    move-object/from16 v0, p0

    .line 96
    iget-object v14, v0, Lcom/scottmain/android/searchlight/SearchLight;->mDrawable:Landroid/graphics/drawable/TransitionDrawable;

    .line 96
    const/4 v5, 0x1

    .line 96
    invoke-virtual {v14, v5}, Landroid/graphics/drawable/TransitionDrawable;->setCrossFadeEnabled(Z)V

    .line 97
    return-void

    .line 70
    :sswitch_0
    move-object/from16 v0, p0

    .line 70
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/SearchLight;->getWindow()Landroid/view/Window;

    move-result-object v16

    .line 70
    .local v16, "$r9":Landroid/view/Window;, ""
    const/16 v5, 0x400

    .line 70
    const/16 v17, 0x400

    .line 70
    move-object/from16 v0, v16

    .line 70
    move/from16 v1, v17

    .line 70
    invoke-virtual {v0, v5, v1}, Landroid/view/Window;->setFlags(II)V

    .line 72
    const v5, 0x7f030000

    .line 72
    move-object/from16 v0, p0

    .line 72
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->setContentView(I)V

    const v5, 0x7f050003

    move-object/from16 v0, p0

    iput v5, v0, Lcom/scottmain/android/searchlight/SearchLight;->mCurrentMode:I

    goto :goto_1

    .line 76
    :sswitch_1
    move-object/from16 v0, p0

    .line 76
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/SearchLight;->getWindow()Landroid/view/Window;

    move-result-object v16

    .line 76
    const/16 v5, 0x400

    .line 76
    const/16 v17, 0x400

    .line 76
    move-object/from16 v0, v16

    .line 76
    move/from16 v1, v17

    .line 76
    invoke-virtual {v0, v5, v1}, Landroid/view/Window;->setFlags(II)V

    .line 78
    const v5, 0x7f030002

    .line 78
    move-object/from16 v0, p0

    .line 78
    invoke-virtual {v0, v5}, Lcom/scottmain/android/searchlight/SearchLight;->setContentView(I)V

    .line 79
    const v5, 0x7f050002

    .line 79
    move-object/from16 v0, p0

    .line 79
    iput v5, v0, Lcom/scottmain/android/searchlight/SearchLight;->mCurrentMode:I

    goto/32 :goto_1

    nop

    :sswitch_data_0
    .sparse-switch
        0x7f050002 -> :sswitch_1
        0x7f050003 -> :sswitch_0
    .end sparse-switch
    .end local v9    # "$r5":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    .end local v11    # "$r6":Landroid/widget/ImageButton;, ""
    .end local v13    # "$r7":Landroid/graphics/drawable/Drawable;, ""
    .end local v16    # "$r9":Landroid/view/Window;, ""
    .end local v7    # "$r3":Landroid/content/SharedPreferences;, ""
    .end local v14    # "$r8":Landroid/graphics/drawable/TransitionDrawable;, ""
    .end local v8    # "$r4":Landroid/view/View;, ""
    .end local v2    # "$r2":Landroid/content/Intent;, ""
    .end local v3    # "$i0":I, ""
    .end local v6    # "$i1":I, ""
.end method

.method protected onCreateDialog(I)Landroid/app/Dialog;
    .locals 6
    .param p1, "id"    # I

    sparse-switch p1, :sswitch_data_0

    goto :goto_0

    .line 183
    :goto_0
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreateDialog(I)Landroid/app/Dialog;

    move-result-object v0

    .local v0, "$r3":Landroid/app/Dialog;, ""
    return-object v0

    .line 173
    :sswitch_0
    new-instance v1, Landroid/app/AlertDialog$Builder;

    .line 173
    .local v1, "$r1":Landroid/app/AlertDialog$Builder;, ""
    invoke-direct {v1, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    .line 174
    const v3, 0x7f060001

    .line 174
    invoke-virtual {v1, v3}, Landroid/app/AlertDialog$Builder;->setMessage(I)Landroid/app/AlertDialog$Builder;

    move-result-object v2

    .line 174
    .local v2, "$r4":Landroid/app/AlertDialog$Builder;, ""
    const/4 v3, 0x0

    .line 174
    invoke-virtual {v2, v3}, Landroid/app/AlertDialog$Builder;->setCancelable(Z)Landroid/app/AlertDialog$Builder;

    move-result-object v2

    new-instance v4, Lcom/scottmain/android/searchlight/SearchLight$1;

    .line 174
    .local v4, "$r2":Lcom/scottmain/android/searchlight/SearchLight$1;, ""
    invoke-direct {v4, p0}, Lcom/scottmain/android/searchlight/SearchLight$1;-><init>(Lcom/scottmain/android/searchlight/SearchLight;)V

    .line 174
    const v3, 0x7f060002

    .line 174
    invoke-virtual {v2, v3, v4}, Landroid/app/AlertDialog$Builder;->setNeutralButton(ILandroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    .line 181
    invoke-virtual {v1}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;

    move-result-object v5

    .local v5, "$r5":Landroid/app/AlertDialog;, ""
    return-object v5

    :sswitch_data_0
    .sparse-switch
        0x7f050000 -> :sswitch_0
    .end sparse-switch
    .end local v5    # "$r5":Landroid/app/AlertDialog;, ""
    .end local v2    # "$r4":Landroid/app/AlertDialog$Builder;, ""
    .end local v1    # "$r1":Landroid/app/AlertDialog$Builder;, ""
    .end local v0    # "$r3":Landroid/app/Dialog;, ""
    .end local v4    # "$r2":Lcom/scottmain/android/searchlight/SearchLight$1;, ""
.end method

.method public onCreateOptionsMenu(Landroid/view/Menu;)Z
    .locals 2
    .param p1, "menu"    # Landroid/view/Menu;

    .line 189
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->getMenuInflater()Landroid/view/MenuInflater;

    move-result-object v0

    .line 190
    .local v0, "$r2":Landroid/view/MenuInflater;, ""
    const v1, 0x7f070000

    .line 190
    invoke-virtual {v0, v1, p1}, Landroid/view/MenuInflater;->inflate(ILandroid/view/Menu;)V

    const/4 v1, 0x1

    return v1
    .end local v0    # "$r2":Landroid/view/MenuInflater;, ""
.end method

.method public onOptionsItemSelected(Landroid/view/MenuItem;)Z
    .locals 7
    .param p1, "item"    # Landroid/view/MenuItem;

    .line 206
    invoke-interface {p1}, Landroid/view/MenuItem;->getItemId()I

    move-result v0

    .local v0, "$i0":I, ""
    sparse-switch v0, :sswitch_data_0

    goto :goto_0

    .line 229
    :goto_0
    invoke-super {p0, p1}, Landroid/app/Activity;->onOptionsItemSelected(Landroid/view/MenuItem;)Z

    move-result v1

    .local v1, "$z0":Z, ""
    return v1

    .line 208
    :sswitch_0
    new-instance v2, Landroid/content/Intent;

    .line 208
    .local v2, "$r2":Landroid/content/Intent;, ""
    const-class v3, Lcom/scottmain/android/searchlight/SearchLight;

    .line 208
    invoke-direct {v2, p0, v3}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 209
    const-string v4, "mode_type"

    .line 209
    const v5, 0x7f050003

    .line 209
    invoke-virtual {v2, v4, v5}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 210
    iget-object v6, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 210
    .local v6, "$r3":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v6}, Lcom/scottmain/android/searchlight/PreviewSurface;->releaseCamera()V

    .line 211
    invoke-virtual {p0, v2}, Lcom/scottmain/android/searchlight/SearchLight;->startActivity(Landroid/content/Intent;)V

    .line 212
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->finish()V

    const/4 v5, 0x1

    return v5

    .line 215
    :sswitch_1
    new-instance v2, Landroid/content/Intent;

    .line 215
    const-class v3, Lcom/scottmain/android/searchlight/SearchLight;

    .line 215
    invoke-direct {v2, p0, v3}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 216
    const-string v4, "mode_type"

    .line 216
    const v5, 0x7f050002

    .line 216
    invoke-virtual {v2, v4, v5}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 217
    iget-object v6, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 217
    invoke-virtual {v6}, Lcom/scottmain/android/searchlight/PreviewSurface;->releaseCamera()V

    .line 218
    invoke-virtual {p0, v2}, Lcom/scottmain/android/searchlight/SearchLight;->startActivity(Landroid/content/Intent;)V

    .line 219
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->finish()V

    const/4 v5, 0x1

    return v5

    .line 222
    :sswitch_2
    new-instance v2, Landroid/content/Intent;

    .line 222
    const-class v3, Lcom/scottmain/android/searchlight/SearchLight;

    .line 222
    invoke-direct {v2, p0, v3}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 223
    const-string v4, "mode_type"

    .line 223
    const v5, 0x7f050001

    .line 223
    invoke-virtual {v2, v4, v5}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 224
    iget-object v6, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 224
    invoke-virtual {v6}, Lcom/scottmain/android/searchlight/PreviewSurface;->releaseCamera()V

    .line 225
    invoke-virtual {p0, v2}, Lcom/scottmain/android/searchlight/SearchLight;->startActivity(Landroid/content/Intent;)V

    .line 226
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->finish()V

    const/4 v5, 0x1

    return v5

    nop

    :sswitch_data_0
    .sparse-switch
        0x7f050007 -> :sswitch_2
        0x7f050008 -> :sswitch_0
        0x7f050009 -> :sswitch_1
    .end sparse-switch
    .end local v6    # "$r3":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    .end local v1    # "$z0":Z, ""
    .end local v0    # "$i0":I, ""
    .end local v2    # "$r2":Landroid/content/Intent;, ""
.end method

.method public onOptionsMenuClosed(Landroid/view/Menu;)V
    .locals 1
    .param p1, "menu"    # Landroid/view/Menu;

    .line 199
    invoke-super {p0, p1}, Landroid/app/Activity;->onOptionsMenuClosed(Landroid/view/Menu;)V

    const/4 v0, 0x1

    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->skipAnimate:Z

    .line 201
    return-void
.end method

.method protected onPause()V
    .locals 2

    .line 125
    invoke-super {p0}, Landroid/app/Activity;->onPause()V

    .line 126
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/SearchLight;->turnOff()V

    .line 127
    iget-object v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 127
    .local v0, "$r1":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/PreviewSurface;->releaseCamera()V

    const/4 v1, 0x1

    iput-boolean v1, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .line 129
    return-void
    .end local v0    # "$r1":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
.end method

.method public onPrepareOptionsMenu(Landroid/view/Menu;)Z
    .locals 6
    .param p1, "menu"    # Landroid/view/Menu;

    .line 235
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->getIntent()Landroid/content/Intent;

    move-result-object v0

    .line 235
    .local v0, "$r2":Landroid/content/Intent;, ""
    const-string v2, "mode_type"

    .line 235
    const v3, 0x7f050001

    .line 235
    invoke-virtual {v0, v2, v3}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v1

    .local v1, "$i0":I, ""
    sparse-switch v1, :sswitch_data_0

    goto :goto_0

    .line 248
    :goto_0
    const v3, 0x7f050008

    .line 248
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 248
    .local v4, "$r3":Landroid/view/MenuItem;, ""
    const/4 v3, 0x1

    .line 248
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 249
    const v3, 0x7f050007

    .line 249
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 249
    const/4 v3, 0x0

    .line 249
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 250
    const v3, 0x7f050009

    .line 250
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 250
    const/4 v3, 0x1

    .line 250
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 253
    :goto_1
    invoke-super {p0, p1}, Landroid/app/Activity;->onPrepareOptionsMenu(Landroid/view/Menu;)Z

    move-result v5

    .local v5, "$z0":Z, ""
    return v5

    .line 237
    :sswitch_0
    const v3, 0x7f050008

    .line 237
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 237
    const/4 v3, 0x0

    .line 237
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 238
    const v3, 0x7f050007

    .line 238
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 238
    const/4 v3, 0x1

    .line 238
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 239
    const v3, 0x7f050009

    .line 239
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 239
    const/4 v3, 0x1

    .line 239
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    goto :goto_1

    .line 242
    :sswitch_1
    const v3, 0x7f050008

    .line 242
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 242
    const/4 v3, 0x1

    .line 242
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 243
    const v3, 0x7f050007

    .line 243
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 243
    const/4 v3, 0x1

    .line 243
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    .line 244
    const v3, 0x7f050009

    .line 244
    invoke-interface {p1, v3}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v4

    .line 244
    const/4 v3, 0x0

    .line 244
    invoke-interface {v4, v3}, Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;

    goto :goto_1

    nop

    :sswitch_data_0
    .sparse-switch
        0x7f050002 -> :sswitch_1
        0x7f050003 -> :sswitch_0
    .end sparse-switch
    .end local v1    # "$i0":I, ""
    .end local v0    # "$r2":Landroid/content/Intent;, ""
    .end local v5    # "$z0":Z, ""
    .end local v4    # "$r3":Landroid/view/MenuItem;, ""
.end method

.method protected onResume()V
    .locals 2

    .line 133
    invoke-super {p0}, Landroid/app/Activity;->onResume()V

    .line 134
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 135
    iget-object v1, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 135
    .local v1, "$r1":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v1}, Lcom/scottmain/android/searchlight/PreviewSurface;->initCamera()V

    .line 137
    :cond_0
    return-void
    .end local v1    # "$r1":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    .end local v0    # "$z0":Z, ""
.end method

.method protected onStop()V
    .locals 5

    .line 141
    invoke-super {p0}, Landroid/app/Activity;->onStop()V

    .line 142
    const/4 v0, 0x0

    .line 142
    iput-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .line 145
    const/4 v0, 0x0

    .line 145
    invoke-virtual {p0, v0}, Lcom/scottmain/android/searchlight/SearchLight;->getPreferences(I)Landroid/content/SharedPreferences;

    move-result-object v1

    .line 146
    .local v1, "$r1":Landroid/content/SharedPreferences;, ""
    invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v2

    .local v2, "$r2":Landroid/content/SharedPreferences$Editor;, ""
    iget v3, p0, Lcom/scottmain/android/searchlight/SearchLight;->mCurrentMode:I

    .line 147
    .local v3, "$i0":I, ""
    const-string v4, "mode_type"

    .line 147
    invoke-interface {v2, v4, v3}, Landroid/content/SharedPreferences$Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences$Editor;

    .line 148
    invoke-interface {v2}, Landroid/content/SharedPreferences$Editor;->commit()Z

    .line 149
    return-void
    .end local v3    # "$i0":I, ""
    .end local v1    # "$r1":Landroid/content/SharedPreferences;, ""
    .end local v2    # "$r2":Landroid/content/SharedPreferences$Editor;, ""
.end method

.method public onWindowFocusChanged(Z)V
    .locals 10
    .param p1, "hasFocus"    # Z

    .line 153
    invoke-super {p0, p1}, Landroid/app/Activity;->onWindowFocusChanged(Z)V

    .line 155
    invoke-virtual {p0}, Lcom/scottmain/android/searchlight/SearchLight;->getIntent()Landroid/content/Intent;

    move-result-object v0

    .line 155
    .local v0, "$r1":Landroid/content/Intent;, ""
    const-string v2, "mode_type"

    .line 155
    const v3, 0x7f050001

    .line 155
    invoke-virtual {v0, v2, v3}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v1

    .local v1, "$i0":I, ""
    if-eqz p1, :cond_0

    iget-boolean v4, p0, Lcom/scottmain/android/searchlight/SearchLight;->skipAnimate:Z

    .local v4, "$z1":Z, ""
    if-nez v4, :cond_0

    const v3, 0x7f050003

    if-ne v1, v3, :cond_0

    .line 157
    const v3, 0x7f050005

    .line 157
    invoke-virtual {p0, v3}, Lcom/scottmain/android/searchlight/SearchLight;->findViewById(I)Landroid/view/View;

    move-result-object v5

    .local v5, "$r2":Landroid/view/View;, ""
    move-object v7, v5

    check-cast v7, Landroid/widget/Button;

    move-object v6, v7

    .line 158
    .local v6, "$r3":Landroid/widget/Button;, ""
    const v3, 0x7f040000

    .line 158
    invoke-static {p0, v3}, Landroid/view/animation/AnimationUtils;->loadAnimation(Landroid/content/Context;I)Landroid/view/animation/Animation;

    move-result-object v8

    .line 159
    .local v8, "$r4":Landroid/view/animation/Animation;, ""
    invoke-virtual {v6, v8}, Landroid/widget/Button;->startAnimation(Landroid/view/animation/Animation;)V

    .line 161
    :cond_0
    const/4 v3, 0x0

    .line 161
    iput-boolean v3, p0, Lcom/scottmain/android/searchlight/SearchLight;->skipAnimate:Z

    if-eqz p1, :cond_1

    iget-boolean p1, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .local p1, "$z0":Z, ""
    if-eqz p1, :cond_1

    .line 164
    iget-object v9, p0, Lcom/scottmain/android/searchlight/SearchLight;->mSurface:Lcom/scottmain/android/searchlight/PreviewSurface;

    .line 164
    .local v9, "$r5":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    invoke-virtual {v9}, Lcom/scottmain/android/searchlight/PreviewSurface;->startPreview()V

    .line 165
    const/4 v3, 0x0

    .line 165
    iput-boolean v3, p0, Lcom/scottmain/android/searchlight/SearchLight;->paused:Z

    .line 167
    :cond_1
    return-void
    .end local v8    # "$r4":Landroid/view/animation/Animation;, ""
    .end local v5    # "$r2":Landroid/view/View;, ""
    .end local v4    # "$z1":Z, ""
    .end local v6    # "$r3":Landroid/widget/Button;, ""
    .end local v9    # "$r5":Lcom/scottmain/android/searchlight/PreviewSurface;, ""
    .end local v0    # "$r1":Landroid/content/Intent;, ""
    .end local v1    # "$i0":I, ""
    .end local p1    # "$z0":Z, ""
.end method

.method public toggleLight(Landroid/view/View;)V
    .locals 1
    .param p1, "v"    # Landroid/view/View;

    .line 100
    iget-boolean v0, p0, Lcom/scottmain/android/searchlight/SearchLight;->on:Z

    .local v0, "$z0":Z, ""
    if-eqz v0, :cond_0

    .line 101
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/SearchLight;->turnOff()V

    .line 105
    return-void

    .line 103
    :cond_0
    invoke-direct {p0}, Lcom/scottmain/android/searchlight/SearchLight;->turnOn()V

    return-void
    .end local v0    # "$z0":Z, ""
.end method
