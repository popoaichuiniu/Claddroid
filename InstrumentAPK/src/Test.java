import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.cheng.MessageResolver;

public class Test {
	public static void main(String[] args){
		String mn = "a";
		String signature = "(Landroid/content/Context;Lcom/devuni/helper/i;IIII[F)Landroid/widget/TextView;";
		String sig = MessageResolver.toSootMethodSignature(signature, mn);
		System.out.println(sig);
	}
}
