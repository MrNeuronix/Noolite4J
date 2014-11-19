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

package ru.iris.noolite4j.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.DataFormat;

/**
 * Ethernet-шлюз PR1132
 * @link http://www.noo.com.by/Ethernet_PR1132.html
 */

public class PR1132 {

    private static final Logger LOGGER = LoggerFactory.getLogger(PR1132.class.getName());
    private byte availableTXChannels = 32;
    private byte availableRXChannels = 4;
    private static String url;

    /**
     * Устанавливает адрес PR1132
     */
    public static void setHost(String url) {

        LOGGER.debug("Устанавливается адрес для устройства PR1132: " + url);
        PR1132.url = url;
    }

    public static String getHost()
    {
        return url;
    }

    /**
     * Включает силовой блок на определеном канале
     * @param channel канал включаемой нагрузки
     * @return успешно или нет
     */
    public boolean turnOn(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Включается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.TURN_ON);

        return command.send();
    }

    /**
     * Медленно включает диммируемую нагрузку
     * @param channel канал включаемой нагрузки
     * @return успешно или нет
     */
    public boolean slowTurnOn(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Включается (медленно) устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.SLOW_TURN_ON);

        return command.send();
    }

    /**
     * Медленно выключает диммируемую нагрузку
     * @param channel канал выключаемой нагрузки
     * @return успешно или нет
     */
    public boolean slowTurnOff(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Выключается (медленно) устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.SLOW_TURN_OFF);

        return command.send();
    }

    /**
     * Переключает нагрузку (вкл/выкл)
     * @param channel канал переключаемой нагрузки
     * @return успешно или нет
     */
    public boolean toggle(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Переключается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.SWITCH);

        return command.send();
    }

    /**
     * Запускает плавное изменение яркости в обратном направлении
     * @param channel канал нагрузки
     * @return успешно или нет
     */
    public boolean revertSlowTurn(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Плавное изменение яркости в обратном направлении на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.REVERT_SLOW_TURN);

        return command.send();
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
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Устанавливается яркость для кажого цвета RGB-контроллера на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setFmt(DataFormat.FOUR_BYTE);
        command.setCmd(CommandType.SET_LEVEL);
        command.setD0(R);
        command.setD1(G);
        command.setD2(B);

        return command.send();
    }

    /**
     * Вызвать записанный сценарий
     * @return успешно или нет
     */
    public boolean callScene()
    {
        LOGGER.debug("Вызывается записанный сценарий");

        HTTPCommand command = new HTTPCommand();;
        command.setCmd(CommandType.RUN_SCENE);

        return command.send();
    }

    /**
     * Записать сценарий
     * @return успешно или нет
     */
    public boolean recordScene()
    {
        LOGGER.debug("Записывается сценарий");

        HTTPCommand command = new HTTPCommand();
        command.setCmd(CommandType.RECORD_SCENE);

        return command.send();
    }

    /**
     * Остановить регулировку яркости
     * @return успешно или нет
     */
    public boolean stopDimBright(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Останавливается регулировка яркости на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.STOP_DIM_BRIGHT);

        return command.send();
    }

    /**
     * Включение плавного перебора цвета
     * @return успешно или нет
     */
    public boolean slowRGBChange(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Включение плавного перебора цвета на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setFmt(DataFormat.LED);
        command.setCmd(CommandType.SLOW_RGB_CHANGE);

        return command.send();
    }

    /**
     * Переключение цвета
     * @return успешно или нет
     */
    public boolean colorChange(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Переключение цвета на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setFmt(DataFormat.LED);
        command.setCmd(CommandType.SWITCH_COLOR);

        return command.send();
    }

    /**
     * Переключение режима работы RGB-контроллера
     * @return успешно или нет
     */
    public boolean switchRGBMode(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Переключение режима работы RGB-контроллера на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setFmt(DataFormat.LED);
        command.setCmd(CommandType.SWITCH_MODE);

        return command.send();
    }

    /**
     * Переключение скорости эффекта в режиме работы RGB-контроллера
     * @return успешно или нет
     */
    public boolean switchSpeedRGBMode(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Переключение скорости эффекта в режиме работы RGB-контроллера на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setFmt(DataFormat.LED);
        command.setCmd(CommandType.SWITCH_SPEED_MODE);

        return command.send();
    }

    /**
     * Выключает силовой блок на определеном канале
     * @param channel канал выключаемой нагрузки
     * @return успешно или нет
     */
    public boolean turnOff(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        LOGGER.debug("Выключается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.TURN_OFF);

        return command.send();
    }

    /**
     * Устанавливает уровень на диммируемом силовом блоке на определеном канале
     * @param channel канал диммера
     * @return успешно или нет
     */
    public boolean setLevel(byte channel, byte level)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.SET_LEVEL);

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

        command.setBr(level);

        return command.send();
    }

    /**
     * Привзяка устройства к PC11xx на определенный канал
     * @param channel канал, на который будет привязано устройство
     * @return успешно или нет
     */
    public boolean bindChannel(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.BIND);

        return command.send();
    }

    /**
     * Отвзяка устройства PC11xx от определенного канала
     * @param channel канал, от которого будет отвязано устройство
     * @return успешно или нет
     */
    public boolean unbindChannel(byte channel)
    {
        if(channel >= availableTXChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableTXChannels);
            return false;
        }

        HTTPCommand command = new HTTPCommand();
        command.setChannel(channel);
        command.setCmd(CommandType.UNBIND);

        return command.send();
    }
}
