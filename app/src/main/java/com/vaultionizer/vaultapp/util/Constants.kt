package com.vaultionizer.vaultapp.util

object Constants {

    /**
     * Application
     */
    const val WORK_MANAGER_SCHEDULER_LIMIT = 3
    const val DEFAULT_PROTOCOL = "https"

    /**
     * Meta
     */
    const val MIN_SERVER_VERSION = "0.1"
    const val SESSION_TOKEN_LIFETIME = 30 * 60 * 1000

    /**
     * Internal File System
     */
    const val VN_FILE_SUFFIX = "evn"

    /**
     * Logging
     */
    const val VN_TAG = "Vault"

    /**
     * Crypto
     */
    const val VN_KEY_PREFIX = "vaultionizer_"
    const val VN_KEYSTORE_PROVIDER = "AndroidKeyStore"
    const val VN_KEY_TRANSFER_SIZE = 32

    /**
     * WorkManager
     */
    const val WORKER_TAG_FILE = "FILE_WORKER"
    const val WORKER_TAG_REFERENCE_FILE = "REFERENCE_FILE_WORKER"
    const val WORKER_TAG_ENCRYPTION = "ENCRYPTION_WORKER"
    const val WORKER_TAG_DECRYPTION = "DECRYPTION_WORKER"
    const val WORKER_TAG_DOWNLOAD = "DOWNLOAD_WORKER"
    const val WORKER_TAG_UPLOAD = "UPLOAD_WORKER"
    const val WORKER_SYNC_REQUEST_ID = "SYNC_REQUEST_ID"
    const val WORKER_FILE_ID = "FILE_ID"
    const val WORKER_SPACE_ID = "SPACE_ID"
    const val WORKER_TAG_FILE_ID_TEMPLATE = "FILE_WORKER_FILE_ID_%d"
    const val WORKER_TAG_FILE_ID_TEMPLATE_BEGIN = "FILE_WORKER_FILE_ID_"
    const val WORKER_FILE_UNIQUE_NAME_TEMPLATE = "FILE_%d"
}