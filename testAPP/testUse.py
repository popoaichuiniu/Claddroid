import subprocess
def execuateCmd(cmd):
    #status,output=subprocess.getstatusoutput(cmd);
    proc=subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    outs, errs = proc.communicate()
    return proc.returncode,str(outs, encoding = "utf-8")+"##"+str(errs, encoding = "utf-8"),proc

print("python start")
cmd='guake -e  "adb logcat *:E|tee -a /home/zms/logger_file/testlog/error.log"'
status,output,proc=execuateCmd(cmd)
print("python stop")
print(status)
print(output)