// Strategy Pattern Example - 定义一系列算法，把它们一个个封装起来，并且使它们可相互替换

// 策略接口
interface PaymentStrategy {
    void pay(double amount);
}

// 具体策略 - 信用卡支付
class CreditCardStrategy implements PaymentStrategy {
    private String name;
    private String cardNumber;
    
    public CreditCardStrategy(String name, String cardNumber) {
        this.name = name;
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用信用卡支付 $" + amount);
        System.out.println("持卡人: " + name);
        System.out.println("卡号: " + cardNumber);
    }
}

// 具体策略 - 支付宝支付
class AlipayStrategy implements PaymentStrategy {
    private String mobileNumber;
    
    public AlipayStrategy(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用支付宝支付 $" + amount);
        System.out.println("手机号: " + mobileNumber);
        System.out.println("跳转到支付宝APP...");
    }
}

// 具体策略 - 微信支付
class WechatPayStrategy implements PaymentStrategy {
    private String openid;
    
    public WechatPayStrategy(String openid) {
        this.openid = openid;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用微信支付 $" + amount);
        System.out.println("OpenID: " + openid);
        System.out.println("打开微信扫描二维码...");
    }
}

// 上下文类
class ShoppingCart {
    private PaymentStrategy paymentStrategy;
    private double totalAmount;
    
    public ShoppingCart() {
        this.totalAmount = 0;
    }
    
    public void addItem(double price) {
        totalAmount += price;
        System.out.println("添加商品，价格: $" + price);
    }
    
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }
    
    public void checkout() {
        if (paymentStrategy == null) {
            System.out.println("请选择支付方式！");
            return;
        }
        
        System.out.println("\n开始结账...");
        System.out.println("总金额: $" + totalAmount);
        paymentStrategy.pay(totalAmount);
        System.out.println("支付成功！\n");
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
}

// 排序策略示例
interface SortStrategy {
    void sort(int[] array);
}

class BubbleSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("使用冒泡排序");
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }
}

class QuickSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("使用快速排序");
        quickSort(array, 0, array.length - 1);
    }
    
    private void quickSort(int[] array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }
    
    private int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (array[j] < pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        return i + 1;
    }
}

class Sorter {
    private SortStrategy strategy;
    
    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void sortArray(int[] array) {
        if (strategy != null) {
            strategy.sort(array);
        }
    }
}

public class StrategyPatternExample {
    public static void main(String[] args) {
        System.out.println("=== 策略模式示例 ===\n");
        
        // 购物车支付示例
        System.out.println("1. 购物车支付示例:");
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(29.99);
        cart.addItem(15.50);
        cart.addItem(8.75);
        
        // 使用信用卡支付
        System.out.println("--- 使用信用卡支付 ---");
        PaymentStrategy creditCard = new CreditCardStrategy("张三", "1234-5678-9012-3456");
        cart.setPaymentStrategy(creditCard);
        cart.checkout();
        
        // 切换到支付宝支付
        System.out.println("--- 切换到支付宝支付 ---");
        PaymentStrategy alipay = new AlipayStrategy("138****8888");
        cart.setPaymentStrategy(alipay);
        cart.checkout();
        
        // 排序策略示例
        System.out.println("2. 排序策略示例:");
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};
        
        Sorter sorter = new Sorter();
        
        // 使用冒泡排序
        System.out.println("原始数组: ");
        printArray(numbers);
        sorter.setStrategy(new BubbleSortStrategy());
        sorter.sortArray(numbers);
        System.out.println("冒泡排序后: ");
        printArray(numbers);
        
        // 重新初始化数组
        int[] numbers2 = {64, 34, 25, 12, 22, 11, 90};
        
        // 使用快速排序
        System.out.println("\n原始数组: ");
        printArray(numbers2);
        sorter.setStrategy(new QuickSortStrategy());
        sorter.sortArray(numbers2);
        System.out.println("快速排序后: ");
        printArray(numbers2);
        
        // 策略模式的优势演示
        System.out.println("\n3. 策略模式优势演示:");
        demonstrateStrategyAdvantages();
    }
    
    private static void printArray(int[] array) {
        for (int num : array) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
    private static void demonstrateStrategyAdvantages() {
        System.out.println("策略模式的主要优势:");
        System.out.println("1. 算法可以自由切换");
        System.out.println("2. 避免使用多重条件判断");
        System.out.println("3. 扩展性良好，符合开闭原则");
        System.out.println("4. 算法复用，避免重复代码");
        System.out.println("5. 完美体现面向对象设计原则");
    }
}