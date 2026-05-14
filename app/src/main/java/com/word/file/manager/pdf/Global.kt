package com.word.file.manager.pdf

const val EXTRA_FILE_ITEM = "extra_file_item"
const val EXTRA_RESULT_TEXT = "extra_result_text"
const val EXTRA_DOCUMENT_ACTION_TYPE = "extra_document_action_type"
const val EXTRA_NOTIFICATION_SCENE = "extra_notification_scene"
const val EXTRA_NOTIFICATION_SURFACE = "extra_notification_surface"
const val EXTRA_SHORTCUT_PAGE = "shortcut_entry"
const val EXTRA_FROM_SET = "extra_from_set"
const val SHORTCUT_PAGE_VIEW = "quick_open_files"
const val SHORTCUT_PAGE_SCAN = "quick_create_scan"
const val SHORTCUT_PAGE_UNINSTALL = "quick_exit_guide"
const val ADMOB = "admob"
const val APP_AD_CHANCE = "app_ad_chance"
const val APP_AD_IMPRESSION = "app_ad_impression"
const val APP_AD_IMPRESSION_CLICK = "app_ad_impression_click"
const val AD_POS_ID = "ad_pos_id"

lateinit var app: BaseApp
var isDebug = true
var hasGoSettings: Boolean = false
var hasShownMainNoticeGuide = false
