
/*
 * MIT License
 *
 * Copyright (c) 2017 Liew Jun Tung
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.pandawarrior.app

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlValue

/**
 * Created by jtlie on 3/15/2017.
 */
@XmlRootElement(name = "resources")
class APluralResource() {
    var aPluralList: List<APlural> = ArrayList()
        @XmlElement(name = "plurals") set

    override fun toString(): String {
        return "APluralResounce(aArrayList=$aPluralList)"
    }


}

@XmlRootElement(name = "plurals")
class APlural() {

    var name: String = ""
        @XmlAttribute set

    var aPluralItems: List<APluralItem> = ArrayList()
        @XmlElement(name = "item") set

    override fun toString(): String {
        return "APlural(name='$name', aPluralItems=$aPluralItems)"
    }


}

@XmlRootElement(name = "item")
class APluralItem() {
    var quantity: String = ""
        @XmlAttribute set

    var text: String = ""
        @XmlValue set

    override fun toString(): String {
        return "APluralItem(quantity='$quantity', text='$text')"
    }


}
/*
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <plurals
        name="plural_name">
        <item
            quantity=["zero" | "one" | "two" | "few" | "many" | "other"]
            >text_string</item>
    </plurals>
</resources>
 */