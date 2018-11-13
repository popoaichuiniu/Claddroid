.class Lcom/scottmain/android/searchlight/SearchLight$1;
.super Ljava/lang/Object;
.source "SearchLight.java"

# interfaces
.implements Landroid/content/DialogInterface$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/scottmain/android/searchlight/SearchLight;->onCreateDialog(I)Landroid/app/Dialog;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/scottmain/android/searchlight/SearchLight;


# direct methods
.method constructor <init>(Lcom/scottmain/android/searchlight/SearchLight;)V
    .locals 0

    .line 176
    iput-object p1, p0, Lcom/scottmain/android/searchlight/SearchLight$1;->this$0:Lcom/scottmain/android/searchlight/SearchLight;

    .line 176
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/content/DialogInterface;I)V
    .locals 1
    .param p1, "dialog"    # Landroid/content/DialogInterface;
    .param p2, "id"    # I

    .line 178
    iget-object v0, p0, Lcom/scottmain/android/searchlight/SearchLight$1;->this$0:Lcom/scottmain/android/searchlight/SearchLight;

    .line 178
    .local v0, "$r2":Lcom/scottmain/android/searchlight/SearchLight;, ""
    invoke-virtual {v0}, Lcom/scottmain/android/searchlight/SearchLight;->finish()V

    .line 179
    return-void
    .end local v0    # "$r2":Lcom/scottmain/android/searchlight/SearchLight;, ""
.end method
