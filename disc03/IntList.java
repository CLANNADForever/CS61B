public class IntList {
    public int first;
    public IntList rest;

    public IntList(int f, IntList r) {
        this.first = f;
        this.rest = r;
    }

    /**
     * Rearranges the IntList so that all even-indexed elements appear before all odd-indexed elements.
     * Does this in-place, without creating new nodes.
     * Example: (0, 3, 1, 4, 2, 5) -> (0, 1, 2, 3, 4, 5)
     */
    public static void evenOdd(IntList lst) {
        // Case 1: 如果链表为空或只有一个节点，什么都不用做。
        if (lst == null || lst.rest == null) {
            return;
        }

        // 我们将链表看作是“奇数链”和“偶数链”交织在一起。
        // oddHead 指向奇数链的头部（也就是原始链表的头部）。
        // evenHead 指向偶数链的头部（也就是原始链表的第二个节点）。
        IntList oddHead = lst;
        IntList evenHead = lst.rest;

        // 我们需要两个“漫游”指针，分别在奇数链和偶数链上移动，进行穿针引线的工作。
        // oddPtr 从奇数链的头开始。
        // evenPtr 从偶数链的头开始。
        IntList oddPtr = oddHead;
        IntList evenPtr = evenHead;

        // 循环的条件是：只要偶数链和偶数链的下一个节点都还存在，我们就可以继续拆分。
        // (evenPtr != null) 保证了偶数链本身没走完。
        // (evenPtr.rest != null) 保证了偶数链后面还有一个奇数节点可以连接。
        while (evenPtr != null && evenPtr.rest != null) {
            // 步骤 a: 将奇数指针指向下一个奇数节点，即跳过中间的偶数节点。
            // 原始: odd -> even -> next_odd
            // 之后: odd -> next_odd
            oddPtr.rest = evenPtr.rest;

            // 步骤 b: 奇数指针向前移动到新的位置。
            oddPtr = oddPtr.rest;

            // 步骤 c: 将偶数指针指向下一个偶数节点，即跳过中间的奇数节点。
            // 原始: even -> next_odd -> next_even
            // 之后: even -> next_even
            evenPtr.rest = oddPtr.rest;

            // 步骤 d: 偶数指针向前移动到新的位置。
            evenPtr = evenPtr.rest;
        }

        // 循环结束后，我们已经成功将奇数节点和偶数节点分别串联起来了。
        // oddPtr 现在指向奇数链的最后一个节点。
        // 我们需要将整个偶数链（从 evenHead 开始）连接到奇数链的末尾。
        oddPtr.rest = evenHead;
    }

    public void printList() {
        IntList current = this; // 直接使用 this，避免创建新对象
        while (current != null) {
            System.out.print(current.first + " ");
            current = current.rest;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // 创建测试链表: 0 -> 3 -> 1 -> 4 -> 2 -> 5 -> null
        IntList test1 = new IntList(5, null);
        test1 = new IntList(2, test1);
        test1 = new IntList(4, test1);
        test1 = new IntList(1, test1);
        test1 = new IntList(3, test1);
        test1 = new IntList(0, test1);

        System.out.print("Original list: ");
        test1.printList(); // 期望输出: 0 3 1 4 2 5

        evenOdd(test1);

        System.out.print("Modified list: ");
        test1.printList(); // 期望输出: 0 1 2 3 4 5
    }
}