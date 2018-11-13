#!/usr/bin/env bash
mkdir $1
cp apk_file_no_test_info.txt $1
cp failTest_apk_list $1
cp successTest_apk_list $1
cp /home/zms/logger_file/testlog/*.log $1
cp app_test_status $1
cp too_many_test_app $1
cp temp_intent $1
cp intent_count $1
cp timeUse.txt $1
cp has_process_app_list $1

rm apk_file_no_test_info.txt
rm failTest_apk_list
rm successTest_apk_list
rm /home/zms/logger_file/testlog/*.log
rm app_test_status
rm too_many_test_app
rm temp_intent
rm intent_count
rm timeUse.txt