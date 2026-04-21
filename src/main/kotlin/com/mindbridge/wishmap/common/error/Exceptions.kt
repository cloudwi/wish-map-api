package com.mindbridge.wishmap.common.error

open class BusinessException(message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : BusinessException(message)

class UnauthorizedException(message: String) : BusinessException(message)

class ForbiddenException(message: String) : BusinessException(message)

class DuplicateResourceException(message: String) : BusinessException(message)
