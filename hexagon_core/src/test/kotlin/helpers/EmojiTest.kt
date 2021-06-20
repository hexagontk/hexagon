package helpers

import com.hexagonkt.helpers.Emoji
import com.hexagonkt.helpers.println
import org.junit.jupiter.api.Test

internal class EmojiTest {

    @Test fun `Face costume Emojis are displayed (visual only, no asserts)`() {
        Emoji.FaceCostume.POO.println("POO : ")
        Emoji.FaceCostume.CLOWN.println("CLOWN : ")
        Emoji.FaceCostume.OGRE.println("OGRE : ")
        Emoji.FaceCostume.GOBLIN.println("GOBLIN : ")
        Emoji.FaceCostume.GHOST.println("GHOST : ")
        Emoji.FaceCostume.ALIEN.println("ALIEN : ")
        Emoji.FaceCostume.ALIEN_MONSTER.println("ALIEN_MONSTER : ")
        Emoji.FaceCostume.ROBOT.println("ROBOT : ")
    }
}
