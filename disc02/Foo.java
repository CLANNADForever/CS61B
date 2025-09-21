public class Foo {
    public int x, y;
    public Foo (int x, int y) {
        this.x = x;
        this.y = y;
    }
    public static void switcheroo (Foo a, Foo b) {
        Foo temp = a;
        a = b;
        b = temp;
    }
    public static void fliperoo (Foo a, Foo b) {
        Foo temp = new Foo(a.x, a.y);
        a.x = b.x;
        a.y = b.y;
        b.x = temp.x;
        b.y = temp.y;
    }
    public static void swaperoo (Foo a, Foo b) {
        Foo temp = a;
        a.x = b.x;
        a.y = b.y;
        b.x = temp.x;
        b.y = temp.y;
    }
    public static void main (String[] args) {
        Foo foobar = new Foo(10, 20);
        Foo baz = new Foo(30, 40);
        switcheroo(foobar, baz); // nothing changes
        System.out.printf("foobar.x:%d foobar.y:%d baz.x:%d baz.y:%d\n", foobar.x, foobar.y, baz.x, baz.y);

        fliperoo(foobar, baz);

        System.out.printf("foobar.x:%d foobar.y:%d baz.x:%d baz.y:%d\n", foobar.x, foobar.y, baz.x, baz.y);

        swaperoo(foobar, baz);

        System.out.printf("foobar.x:%d foobar.y:%d baz.x:%d baz.y:%d\n", foobar.x, foobar.y, baz.x, baz.y);
    }


}
