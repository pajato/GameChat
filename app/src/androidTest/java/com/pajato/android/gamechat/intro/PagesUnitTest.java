/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.intro;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.pajato.android.gamechat.intro.Pages.home;
import static com.pajato.android.gamechat.intro.Pages.chat;
import static com.pajato.android.gamechat.intro.Pages.game;
import static com.pajato.android.gamechat.intro.Pages.levels;
import static com.pajato.android.gamechat.intro.Pages.valueOf;
import static com.pajato.android.gamechat.intro.Pages.values;

/**
 * Provide a unit test class in order to achieve acceptable coverage on the Preference class code.
 *
 * Created by pmr on 8/2/17.
 */
public class PagesUnitTest {
    @Before public void setUp() throws Exception { }

    @After public void tearDown() throws Exception { }

    @Test public void testType() {
        // Test the implicit enum methods.
        Assert.assertEquals("The type size is wrong!", 4, values().length);
        Assert.assertEquals("The value is wrong!", home, valueOf("home"));
        Assert.assertEquals("The value is wrong!", chat, valueOf("chat"));
        Assert.assertEquals("The value is wrong!", game, valueOf("game"));
        Assert.assertEquals("The value is wrong!", levels, valueOf("levels"));

    }
}
