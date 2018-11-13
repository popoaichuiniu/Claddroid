time_file=open('timeUse.txt','r')
lines=time_file.readlines()
min=100000
max=-1
sum=0
count=0
for line in lines:
    temp=line.rstrip('\n')
    if(temp !=''):
        tempFloat=float(temp)
        sum=sum+tempFloat
        count=count+1
        if(min>tempFloat):
            min=tempFloat
        if(max<tempFloat):
            max=tempFloat
    else:
        print("sb")
print(count)
print(sum/count)
print(min)
print(max)


