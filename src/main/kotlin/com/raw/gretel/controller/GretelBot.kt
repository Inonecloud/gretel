package com.raw.gretel.controller

import com.raw.gretel.domain.START_DESCRIPTION
import com.raw.gretel.service.EncryptionService
import com.raw.gretel.service.ResponseHandler
import com.raw.gretel.service.UserService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Flag
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy

@Component
class GretelBot(
    env: Environment,
    userService: UserService,
    encryptionService: EncryptionService
) : AbilityBot(env.getProperty("bot.token"), env.getProperty("bot.name")) {
    private val responseHandler: ResponseHandler = ResponseHandler(silent, db, userService, encryptionService)
    override fun creatorId(): Long {
        return 1L
    }

    fun startBot(): Ability {
        return Ability.builder()
            .name("start")
            .info(START_DESCRIPTION)
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.replyToStart(ctx) }
            .build()
    }

    fun checkInInBot(): Ability {
        return Ability.builder()
            .name("checkin")
            .info("")
            .locality(Locality.GROUP)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.checkIn(ctx) }
            .build()
    }

    fun sos(): Ability {
        return Ability.builder()
            .name("sos")
            .info("")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                responseHandler.sos(ctx.user().id)
            }
            .build()
    }

    fun hide(): Ability{
        return Ability.builder()
            .name("hide")
            .info("")
            .locality(Locality.GROUP)
            .privacy(Privacy.GROUP_ADMIN)
            .action { ctx ->
                responseHandler.hideByDemand(ctx.arguments()[0])
            }
            .build()
    }

    fun invite():Ability {
        return Ability.builder()
            .name("invite")
            .info("")
            .locality(Locality.GROUP)
            .privacy(Privacy.GROUP_ADMIN)
            .action { ctx ->
                responseHandler.invite(ctx.arguments()[0], ctx.chatId())
            }
            .build()
    }

    fun approveJoinRequest():Ability{
        return Ability.builder()
            .name(DEFAULT)
            .flag(Flag.CHAT_JOIN_REQUEST)
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx -> responseHandler.reviewJoinRequest(ctx.chatId(), ctx.user().id)}
            .build()
    }

    fun forgetMe(): Ability {
        return Ability.builder()
            .name("forget")
            .info("")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.forgetMe(ctx.user().id) }
            .build()
    }

}