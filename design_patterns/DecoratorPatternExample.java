// Decorator Pattern Example - 动态地给对象添加功能

import java.io.*;

// 组件接口
interface Coffee {
    String getDescription();
    double getCost();
}

// 具体组件
class SimpleCoffee implements Coffee {
    @Override
    public String getDescription() {
        return "简单咖啡";
    }
    
    @Override
    public double getCost() {
        return 2.0;
    }
}

// 装饰器基类
abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
    
    @Override
    public abstract String getDescription();
    
    @Override
    public abstract double getCost();
}

// 具体装饰器 - 牛奶
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + " + 牛奶";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.5;
    }
}

// 具体装饰器 - 糖
class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + " + 糖";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.2;
    }
}

// 具体装饰器 - 巧克力
class ChocolateDecorator extends CoffeeDecorator {
    public ChocolateDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + " + 巧克力";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.8;
    }
}

// Java I/O流的实际应用示例
class StreamDecoratorExample {
    public static void demonstrateStreamDecorators() {
        System.out.println("\n=== Java I/O流装饰器示例 ===");
        
        try {
            // 创建文件输出流（基础组件）
            FileOutputStream fileOut = new FileOutputStream("test.txt");
            
            // 添加缓冲装饰器
            BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut);
            
            // 添加数据输出装饰器
            DataOutputStream dataOut = new DataOutputStream(bufferedOut);
            
            // 写入数据
            dataOut.writeInt(123);
            dataOut.writeUTF("Hello World");
            dataOut.writeDouble(3.14159);
            
            dataOut.close(); // 会自动关闭所有包装的流
            
            // 读取数据
            FileInputStream fileIn = new FileInputStream("test.txt");
            BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
            DataInputStream dataIn = new DataInputStream(bufferedIn);
            
            System.out.println("读取的数据:");
            System.out.println("整数: " + dataIn.readInt());
            System.out.println("字符串: " + dataIn.readUTF());
            System.out.println("双精度浮点数: " + dataIn.readDouble());
            
            dataIn.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class DecoratorPatternExample {
    public static void main(String[] args) {
        System.out.println("=== 装饰器模式示例 ===\n");
        
        // 基础咖啡
        Coffee simpleCoffee = new SimpleCoffee();
        System.out.println("基础咖啡: " + simpleCoffee.getDescription() + 
                          " - 价格: $" + simpleCoffee.getCost());
        
        // 加牛奶的咖啡
        Coffee milkCoffee = new MilkDecorator(simpleCoffee);
        System.out.println("加牛奶咖啡: " + milkCoffee.getDescription() + 
                          " - 价格: $" + milkCoffee.getCost());
        
        // 加糖的咖啡
        Coffee sugarCoffee = new SugarDecorator(simpleCoffee);
        System.out.println("加糖咖啡: " + sugarCoffee.getDescription() + 
                          " - 价格: $" + sugarCoffee.getCost());
        
        // 多层装饰 - 牛奶+糖+巧克力
        Coffee deluxeCoffee = new ChocolateDecorator(
                              new SugarDecorator(
                              new MilkDecorator(simpleCoffee)));
        System.out.println("豪华咖啡: " + deluxeCoffee.getDescription() + 
                          " - 价格: $" + deluxeCoffee.getCost());
        
        // 动态组合示例
        System.out.println("\n=== 动态组合示例 ===");
        Coffee myCoffee = simpleCoffee;
        String[] additions = {"milk", "sugar", "chocolate"};
        
        for (String addition : additions) {
            switch (addition) {
                case "milk":
                    myCoffee = new MilkDecorator(myCoffee);
                    break;
                case "sugar":
                    myCoffee = new SugarDecorator(myCoffee);
                    break;
                case "chocolate":
                    myCoffee = new ChocolateDecorator(myCoffee);
                    break;
            }
            System.out.println("添加" + addition + "后: " + 
                             myCoffee.getDescription() + " - 价格: $" + myCoffee.getCost());
        }
        
        // 演示Java I/O流装饰器
        StreamDecoratorExample.demonstrateStreamDecorators();
    }
}