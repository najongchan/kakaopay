package com.example.demo.exception

import com.example.demo.entity.ApiError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    class BroadcastNotExist(override val message: String?)
        : Exception(message)
    class BroadcastDataExpired(override val message: String?)
        : Exception(message)
    class UnAuthorizedUser(override val message: String?)
        : Exception(message)
    class UserAlreadyReceivedException(override val message: String?)
        : Exception(message)
    class OwnerReceiveException(override val message: String?)
        : Exception(message)
    class RoomMemberOnlyException(override val message: String?)
        : Exception(message)
    class SplitsExhaustedException(override val message: String?)
        : Exception(message)
    class SplitsExpiredException(override val message: String?)
        : Exception(message)
    class TokenDuplicatedException(override val message: String?)
        : Exception(message)

    @ExceptionHandler(value = [(BroadcastNotExist::class)])
    fun handleBroadcastNotExist(ex: BroadcastNotExist,request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [(BroadcastDataExpired::class)])
    fun handleBroadcastDataExpired(ex: BroadcastDataExpired,request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [(UnAuthorizedUser::class)])
    fun handleUnAuthorizedUser(ex: UnAuthorizedUser,request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [(UserAlreadyReceivedException::class)])
    fun handleUserAlreadyReceived(ex: UserAlreadyReceivedException,request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [(OwnerReceiveException::class)])
    fun handleOwnerReceive(ex: OwnerReceiveException,request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [(RoomMemberOnlyException::class)])
    fun handleRoomMemberOnly(ex: RoomMemberOnlyException, request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [(SplitsExhaustedException::class)])
    fun handleSplitsExhausted(ex: SplitsExhaustedException, request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [(SplitsExpiredException::class)])
    fun handleSplitsExpired(ex: SplitsExpiredException, request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [(TokenDuplicatedException::class)])
    fun handleTokenDuplicated(ex: TokenDuplicatedException, request: WebRequest)
            : ResponseEntity<ApiError> {
        val errorDetails = ApiError(Date(), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
