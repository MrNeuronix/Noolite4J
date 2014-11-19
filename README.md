Noolite4J
===============
Java API к протоколу Noolite

Состояние сборки: [![Build Status](https://travis-ci.org/Neuronix2/Noolite4J.png?branch=master)](https://travis-ci.org/Neuronix2/Noolite4J)

Сборка
-----------------
Для того, чтобы собрать библиотеку, Вам необходим **Apache Maven**

Выполните в каталоге библиотеки команду:

```
mvn package
```

В результате исполнения в папке **target** вы найдете сам jar-архив с библиотекой и папку **lib** с зависимостями

## Использование
Библиотека предоставляет доступ к USB-адаптерам Noolite для ПК, таким как передатчик PC11xx (PC118, PC1116, PC1132) и приемник RX2164.

Пример использования:

``` java
    public static void main(String[] args) {
        ...
               PC1116 pc = new PC1116();
               RX2164 rx = new RX2164();

                Watcher watcher = new Watcher() {

                   @Override
                   public void onNotification(Notification notification) {

                       System.out.println("----------------------------------");
                       System.out.println("RX2164 получил команду: ");
                       System.out.println("Устройство: " + notification.getChannel());
                       System.out.println("Команда: " + notification.getType().name());
                       System.out.println("Формат данных к команде: " + notification.getDataFormat().name());

                       // Передаются данные с датчика
                       if(notification.getType().equals(CommandType.TEMP_HUMI))
                       {
                           SensorType sensor = (SensorType)notification.getValue("sensorType");
                           System.out.println("Температура: " + notification.getValue("temp"));
                           System.out.println("Влажность: " + notification.getValue("humi"));
                           System.out.println("Тип датчика: " + sensor.name());
                           System.out.println("Состояние батареи: " + notification.getValue("battery"));

                           if(notification.getSensorType().equals(SensorType.PT111))
                           {
                              System.out.println("Обнаружен датчик температуры и влажности");
                           }
                           else if(notification.getSensorType().equals(SensorType.PT112))
                           {
                               System.out.println("Обнаружен датчик температуры");
                           }
                           else if(notification.getSensorType().equals(SensorType.PT112))
                           {
                               System.out.println("Обнаружен датчик движения");
                           }
                       }
                   }
               };

               byte channel = 1;
               byte level = 85;

               pc.open();

               pc.turnOn(channel);
               pc.turnOff(channel);
               pc.setLevel(channel, level);

               pc.close();
               
               rx.open();
               rx.addWatcher(watcher);
               rx.start();
        ...
    }
```

В скомпилированную библиотеку входят программы для тестирования приемника RX2164 и передатчика PC11xx.

Примеры использования передатчика:

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "turnon" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "turnoff" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "setlevel" "1" "55"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "bind" "5"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "unbind" "5"
```

Примеры использования приемника:

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestRX2164
```


## Планы
- Добавить поддержку Ethernet-шлюза PR1132

## Лицензия
Apache 2.0
