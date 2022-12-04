package com.cms.engine.lwjgl.window.util

import com.cms.engine.util.TextJustify
import org.junit.Test

class TextJustifyTest {

    @Test
    fun testFullJustify() {
        val justified = TextJustify.full("The earliest report of the darkness came from Rupert, Vermont, where the sun was already obscured at sunrise. Professor Samuel Williams observed from Cambridge, Massachusetts, \"This extraordinary darkness came on between the hours of 10 and 11 a.m. and continued till the middle of the next night.\"[4] Reverend Ebenezer Parkham of Westborough, Massachusetts, reported peak obscurity to occur \"by 12\", but did not record the time when it first arrived. At Harvard College, the obscuration was reported to arrive at 10:30 a.m., peaking at 12:45 p.m. and abating by 1:10 p.m., but a heavy overcast remained for the rest of the day. The obscuration was reported to have reached Barnstable, Massachusetts, by 2:00 p.m., with peak obscurity reported to have occurred at 5:30 p.m.[3]\n" +
                "\n" +
                "Roosters crowed, woodcocks whistled, and frogs peeped as if night had fallen at 2:00 p.m. in Ipswich, Massachusetts. A witness reported that a strong sooty smell prevailed in the atmosphere, and that rain water had a light film over it that was made up of particles of burnt leaves and ash.[6] Contemporaneous reports also indicated that ash and cinders fell on parts of New Hampshire to a depth of six inches (15 cm).[7]",
            55)
        println("Justified text:\n${justified}")
    }

}