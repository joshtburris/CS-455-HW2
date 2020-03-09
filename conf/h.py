h = []
for line in open('machine_list', 'r').readlines() :
    if line not in h :
        h.append(line)
    else :
        print(line)
