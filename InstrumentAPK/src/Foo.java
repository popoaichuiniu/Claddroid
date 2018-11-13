public class Foo {
	
	public static void main(String[] args) {
		String s = null;
		Foo f = new Foo();
		int a = 7;
		int b = 14;
		int x = (f.bar(21) + a) * b;
		if(null != s){
			System.out.println(s);
		}
	}
	
	public int bar(int n) {
		return n + 42; 
	}
}
