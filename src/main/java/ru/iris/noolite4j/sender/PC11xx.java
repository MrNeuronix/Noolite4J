/*
 * Copyright 2014 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.noolite4j.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.DataFormat;

import java.nio.ByteBuffer;

/**
 * Передатчик комманд PC118 (PC1116, PC1132)
 * Базовый класс
 * Модели отличаются количеством доступных каналов
 * @see <a href="http://www.noo.com.by/adapter-noolite-pc.html">http://www.noo.com.by/adapter-noolite-pc.html</a>
 */

public class PC11xx {

    private static final short VENDOR_ID = 5824; //0x16c0;
    private static final short PRODUCT_ID = 1503; //0x05df;
    private static final Logger LOGGER = LoggerFactory.getLogger(PC11xx.class.getName());
    private final Context context = new Context();
    protected byte availableChannels = 8;
    private byte sendRepeat = 2;
    private final ByteBuffer buf = ByteBuffer.allocateDirect(8);

    /**
     * Пытается найти и открыть HID-устройство PC11xx
     * @throws LibUsbException ошибка LibUSB
     */
    public void open() throws LibUsbException {

        LOGGER.debug("Открывается устройство PC11xx");

        // Инициализируем контекст libusb
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS)
        {
            try
            {
                throw new LibUsbException("Не удалось инициализировать libusb", result);
            }
            catch (LibUsbException e)
            {
                LOGGER.error("Не удалось инициализировать libusb: ", result);
                e.printStackTrace();
            }
        }
    }

    /**
     * Закрывает HID-устройство
     */
    public void close() {
        LOGGER.debug("Закрывается устройство PC11xx");
        LibUsb.exit(context);
    }

    /**
     * Возвращает количество повторов посылки команды
     * @return количество повторов
     */
    public byte getSendRepeat() {
        return sendRepeat;
    }

    /**
     * Устанавливает количество повторов посылки команды
     * @param sendRepeat количество повторов
     */
    public void setSendRepeat(byte sendRepeat) {

        if(sendRepeat < 0 || sendRepeat > 7)
        {
            LOGGER.error("Количество повторов не может быть меньше 0 и больше 7");
            return;
        }

        this.sendRepeat = sendRepeat;
    }

    /**
     * Включает силовой блок на определеном канале
     * @param channel канал включаемой нагрузки
     * @return успешно или нет
     */
    public boolean turnOn(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Включается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.TURN_ON.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Медленно включает диммируемую нагрузку
     * @param channel канал включаемой нагрузки
     * @return успешно или нет
     */
    public boolean slowTurnOn(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Включается (медленно) устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SLOW_TURN_ON.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Медленно выключает диммируемую нагрузку
     * @param channel канал выключаемой нагрузки
     * @return успешно или нет
     */
    public boolean slowTurnOff(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Выключается (медленно) устройство на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SLOW_TURN_OFF.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * переключает нагрузку (вкл/выкл)
     * @param channel канал переключаемой нагрузки
     * @return успешно или нет
     */
    public boolean toggle(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Переключается устройство на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SWITCH.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Запускает плавное изменение яркости в обратном направлении
     * @param channel канал нагрузки
     * @return успешно или нет
     */
    public boolean revertSlowTurn(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Плавное изменение яркости в обратном направлении на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.REVERT_SLOW_TURN.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Устанавливает яркость для кажого цвета RGB-контроллера
     * @param channel канал нагрузки
     * @param R яркость канала 1
     * @param G яркость канала 2
     * @param B яркость канала 3
     * @return успешно или нет
     * TODO Проверить работу
     */
    public boolean setLevelRGB(byte channel, byte R, byte G, byte B)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Устанавливается яркость для кажого цвета RGB-контроллера на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SET_LEVEL.getCode());
        buf.put((byte)DataFormat.FOUR_BYTE.ordinal());
        buf.position(4);
        buf.put(channel);
        buf.put(R);
        buf.put(G);
        buf.put(B);

        writeToHID(buf);

        return true;
    }

    /**
     * Вызвать записанный сценарий
     * @return успешно или нет
     */
    public boolean callScene()
    {
        LOGGER.debug("Вызывается записанный сценарий");

        buf.position(1);
        buf.put((byte) CommandType.RUN_SCENE.getCode());

        writeToHID(buf);

        return true;
    }

    /**
     * Записать сценарий
     * @return успешно или нет
     */
    public boolean recordScene()
    {
        LOGGER.debug("Записывается сценарий");

        buf.position(1);
        buf.put((byte) CommandType.RECORD_SCENE.getCode());

        writeToHID(buf);

        return true;
    }

    /**
     * Остановить регулировку яркости
     * @param channel канал устройства
     * @return успешно или нет
     */
    public boolean stopDimBright(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Останавливается регулировка яркости на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.STOP_DIM_BRIGHT.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Включение плавного перебора цвета
     * @param channel канал устройства
     * @return успешно или нет
     */
    public boolean slowRGBChange(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Включение плавного перебора цвета на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SLOW_RGB_CHANGE.getCode());
        buf.put((byte)DataFormat.LED.ordinal());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Переключение цвета
     * @param channel канал устройства
     * @return успешно или нет
     */
    public boolean colorChange(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Переключение цвета на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SWITCH_COLOR.getCode());
        buf.put((byte)DataFormat.LED.ordinal());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Переключение режима работы RGB-контроллера
     * @param channel канал устройства
     * @return успешно или нет
     */
    public boolean switchRGBMode(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Переключение режима работы RGB-контроллера на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SWITCH_MODE.getCode());
        buf.put((byte)DataFormat.LED.ordinal());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Переключение скорости эффекта в режиме работы RGB-контроллера
     * @param channel канал устройства
     * @return успешно или нет
     */
    public boolean switchSpeedRGBMode(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Переключение скорости эффекта в режиме работы RGB-контроллера на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SWITCH_SPEED_MODE.getCode());
        buf.put((byte)DataFormat.LED.ordinal());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Выключает силовой блок на определеном канале
     * @param channel канал выключаемой нагрузки
     * @return успешно или нет
     */
    public boolean turnOff(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.debug("Выключается устройство на канале {}", channel);

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.TURN_OFF.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Устанавливает уровень на диммируемом силовом блоке на определеном канале
     * @param channel канал диммера
     * @param level выставляемый уровень
     * @return успешно или нет
     */
    public boolean setLevel(byte channel, byte level)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        buf.position(1);
        buf.put((byte) CommandType.SET_LEVEL.getCode());
        buf.put((byte) 1);

        if (level > 100)
        {
            LOGGER.debug("Включается устройство на канале " + (channel+1));
            level = 100;
        }
        else if (level < 0)
        {
            LOGGER.debug("Выключается устройство на канале " + (channel+1));
            level = 0;
        }
        else
        {
            LOGGER.debug("Устанавливается уровень {} на канале {}", level, (channel+1));
        }

        buf.position(5);
        buf.put(level);

        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    /**
     * Привзяка устройства к PC11xx на определенный канал
     * @param channel канал, на который будет привязано устройство
     * @return успешно или нет
     */
    public boolean bindChannel(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        buf.position(1);
        buf.put((byte) CommandType.BIND.getCode());
        buf.position(4);
        buf.put((byte) (channel-1));

        LOGGER.debug("Включен режим привязки для канала " + channel);

        writeToHID(buf);
        return true;
    }

    /**
     * Отвзяка устройства PC11xx от определенного канала
     * @param channel канал, от которого будет отвязано устройство
     * @return успешно или нет
     */
    public boolean unbindChannel(byte channel)
    {
        if(channel >= availableChannels-1) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        buf.position(1);
        buf.put((byte) CommandType.UNBIND.getCode());
        buf.position(4);
        buf.put((byte) (channel-1));

        LOGGER.debug("Включен режим отвязки для канала " + channel);

        writeToHID(buf);
        return true;
    }

    /**
     * Непосредственная запись в устройство
     * @param command буффер посылаемых данных
     */
    private void writeToHID(ByteBuffer command)
    {
        DeviceHandle handle = LibUsb.openDeviceWithVidPid(context, VENDOR_ID, PRODUCT_ID);

        if (handle == null)
        {
            LOGGER.error("Устройство PC11XX не найдено!");
            return;
        }

        if (LibUsb.kernelDriverActive(handle, 0) == 1)
        {
            LibUsb.detachKernelDriver(handle, 0);
        }

        int ret = LibUsb.setConfiguration(handle, 1);

        if (ret != LibUsb.SUCCESS)
        {
            LOGGER.error("Ошибка конфигурирования PC11XX");
            LibUsb.close(handle);
            if (ret == LibUsb.ERROR_BUSY)
            {
                LOGGER.error("Устройство PC11XX занято");
            }
            return;
        }

        LibUsb.claimInterface(handle, 0);

        /**
         * В первом байте устанавливаем количество повторов посылки,
         * битрейт и режим работы адаптера.
         * Из всех этих параметров реально используется только количество повторов
         */

        buf.position(0);
        buf.put((byte)(((sendRepeat & 0x3) << 6) + 0x30));

        LOGGER.debug("PC11XX содержимое буффера: " + command.get(0) + " " + command.get(1) + " " + command.get(2) + " " + command.get(3)
                + " " + command.get(4) + " " + command.get(5) + " " + command.get(6)
                + " " + command.get(7));

        LibUsb.controlTransfer(handle, (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE), (byte) 0x9, (short) 0x300, (short) 0, command, 100L);

        LibUsb.attachKernelDriver(handle, 0);
        LibUsb.close(handle);

        buf.clear();
    }
}
