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
class AArrayResource() {
    var aArrayList: List<AArray> = ArrayList()
        @XmlElement(name = "string-array") set

    override fun toString(): String {
        return "APluralResounce(aArrayList=$aArrayList)"
    }


}

@XmlRootElement(name = "string-array")
class AArray() {

    var name: String = ""
        @XmlAttribute set

    var aArrayItem: List<AArrayItem> = ArrayList()
        @XmlElement(name = "item") set


}

@XmlRootElement(name = "item")
class AArrayItem() {
    var text: String = ""
        @XmlValue set

}
/*
<resources>
    <string-array name="media_names">
        <item>Big Buck Bunny</item>
        <item>Elephants Dream</item>
        <item>Sintel</item>
        <item>Tears of Steel</item>
    </string-array>

    <string-array name="media_uris">
        <item>http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4</item>
        <item>http://archive.org/download/ElephantsDream_277/elephant_dreams_640_512kb.mp4</item>
        <item>http://archive.org/download/Sintel/sintel-2048-stereo_512kb.mp4</item>
        <item>http://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4</item>
    </string-array>
</resources>
 */