###################################################
#   Name:       Aaron Lewis
#   Project:    Quantum Stuff
#   Class:      CS3650
#   Date:       05/14/2019
#   Desc:       Gambling game involving guessing
#               the outcome of measuring qubit(s)
#		        after they are transformed by 
#		        quantum gates
###################################################

import numpy as np
from qiskit import*
from qiskit import IBMQ
import operator
IBMQ.save_account("b2572929500ac8473db45cda686936e797d38a621ad00226a057004391d57ace2a72886341e52d395cd6460bd86ff23e1b2ad88f57649ee455a43aa083003480")

def newCircuit(configuration):      #create a new quantum circuit 
    qReg = QuantumRegister(5, 'q')  #create a set of 5 quantum registers
    cReg = ClassicalRegister(5, 'c') #create a set of 5 classical registers
    qCirc = QuantumCircuit(qReg, cReg) #create a quantum circuit with registers
    if configuration == 0:          #add only 1 hadamard gate per qubit
        for i in range(0, 5):       #puts qubits in superposition state
            qCirc.h(qReg[i])            #50/50 chance 0/1
    
    elif configuration == 1:        #add up to 4 h, x, y, or z gates per qubit
        for i in range(0, 5):       
            numGates = quantumRNG(4) # randomly choose how many gates to apply
            for j in range(0, numGates):
                gate = quantumRNG(4) #randomly determine which gate to apply
                if gate == 0:
                    qCirc.h(qReg[i])    #h gate maps z->x and x-z
                elif gate == 1:
                    qCirc.x(qReg[i])   #x gate is pi rotation about x-axis
                elif gate == 2:
                    qCirc.y(qReg[i])    #y gate is pi rotation about y-axis
                elif gate == 3:
                    qCirc.z(qReg[i])    #z gate is pi rotation about x-axis
    
    elif configuration == 2:        #put qubit in superposition and shift phase
        for i in range(0, 5):
            qCirc.h(qReg[i])            #put qubit in superposition with h gate
            numGates = quantumRNG(2) # randomly choose how many gates to apply
            for j in range(0, numGates):
                gate = quantumRNG(4) #randomly determine which gate to apply
                if gate == 0:
                    qCirc.s(qReg[i])    #s gate rotates about z-axis by pi/2
                elif gate == 1:
                    qCirc.sdg(qReg[i]) #sdg gate rotates about z-axis by -pi/2
                elif gate == 2:
                    qCirc.t(qReg[i])    #t gate rotates about z-axis by pi/4
                elif gate == 3:
                    qCirc.tdg(qReg[i])  #tdg gate rotates about z-axis by -pi/4
                qCirc.h(qReg[i])        #bring qubit out of superposition
                
    elif configuration == 3:        #x, y, z, s, t, h, c-not and toffoli gates
        for i in range(0, 5):       
            addHGate = quantumRNG(2)
            if addHGate:
                qCirc.x(qReg[i])
        for i in range(0, 5):
            numGates = quantumRNG(4) #add up to four gates per qubit
            for j in range(0, numGates):
                gate = quantumRNG(8)
                
                if gate == 0:
                    qCirc.x(qReg[i]) #add x-gate
                elif gate == 1:
                    qCirc.y(qReg[i]) #add y-gate
                elif gate == 2:
                    qCirc.z(qReg[i]) #add z-gate
                elif gate == 3:
                    qCirc.s(qReg[i]) #add s-gate
                elif gate == 4:
                    qCirc.t(qReg[i]) #add t-gate
                elif gate == 5:
                    qCirc.h(qReg[i]) #add h-gate
                elif gate == 6:      
                    source = 0
                    target = 0
                    if i == 0:
                        source = 0
                        target = 1
                    elif i == 1:
                        source = 1
                        target = 2
                    elif i == 2:
                        source = 2
                        target = 1
                    elif i == 3:
                        source = 3
                        target = 4
                    elif i == 4:
                        source = 4
                        target = 3
                    qCirc.cx(qReg[source], qReg[target]) #add c-not gate
                
                elif gate == 7:
                    tofConfig = quantumRNG(4) #randomly select between 4 tofolli gate configs
                    if tofConfig == 0:
                        qCirc.ccx(qReg[0], qReg[1], qReg[2])
                    elif tofConfig == 1:
                        qCirc.ccx(qReg[1], qReg[2], qReg[3])
                    elif tofConfig == 2:
                        qCirc.ccx(qReg[2], qReg[3], qReg[4])
    qCirc.measure(qReg, cReg)
    return qReg, cReg, qCirc

def quantumRNG(rngRange):
    rngRange -= 1
    numQBits = rngRange.bit_length()
    quReg = QuantumRegister(numQBits, 'q')
    clReg = ClassicalRegister(numQBits, 'c')
    rngCirc = QuantumCircuit(quReg, clReg)
    for i in range(0, numQBits):
        rngCirc.h(quReg[i])
    rngCirc.measure(quReg, clReg)
    sim = BasicAer.get_backend('qasm_simulator')
    sim_result = execute(rngCirc, sim).result()
    result_counts = sim_result.get_counts(rngCirc)
    shift = str(max(result_counts.items(), key=operator.itemgetter(1))[0])
    dec = int(shift, 2)
    return dec
 
#driver for game
def main():
    #print game title
    print("Welcome to Quantum Roulette!\n")
    print("This game tests your knowledge of Quantum physics\n")
    print("Use your knowledge of quantum physics to make your best bet")
    
    #init vars
    playAgain = True
    availableFunds = 10
    thisSpin = 0
    betHigh = 0
    betLow = 0
    betEven = 0
    betOdd = 0
    betNumber = 0
    numberSelected = 0
    winnings = 0
    config = 0
    
    #play until quit
    while playAgain:
        print("You currently have "+ str(availableFunds) + " credits\n")
        q, c, circ = newCircuit(config)

        print("A new quantum circuit has been created!\n")
        choice = menu()
        
        #loop this turn until quit or spin
        while choice == 1 or choice == 3 or choice == 4 or choice == 5:
            
            #make a bet
            if choice == 1:
                availableFunds, betHigh, betLow, betEven, betOdd, betNumber, numberSelected = bet(availableFunds, betHigh, betLow, betEven, betOdd, betNumber, numberSelected)
            
            #add credits to bet
            elif choice == 3:
                availableFunds = addCredits(availableFunds)
                print("You now have " + str(availableFunds) + " credits!\n")
            
            #print the rules of the game
            elif choice == 4:
                printRules()
           
            #view the current quantum circuit before you bet
            elif choice == 5:
                viewCircuit(circ)
                
            #select a choice from the menu
            choice = menu()
        
        #simulate the circuit and calculate bet winnings
        if choice == 2:     # spin
            sim = BasicAer.get_backend('qasm_simulator')
            sim_result = execute(circ, sim).result()     #run simulation of circuit
            result_counts = sim_result.get_counts(circ)  #store counts of each outcome
            mostFreq = str(max(result_counts.items(), key=operator.itemgetter(1))[0]) #retrieve most freq outcome
            thisSpin = int(mostFreq, 2) #convert most frequent outcome to decimal
            
            #calculate winnings based on bets
            winnings = calcWinnings(thisSpin, betHigh, betLow, betEven, betOdd, betNumber, numberSelected)
            print("The qubits landed on " + str(thisSpin) + "\n")
            
            #issue winnings and reset bets
            if winnings == 0:
               print("You didn't win any bets!\n")
            else:
                print("You won " + str(winnings) + " credits!\n")
                availableFunds += winnings
                winnings = 0
            betHigh = 0
            betLow = 0
            betEven = 0
            betOdd = 0
            betNumber = 0
            numberSelected = 0
            
            #prep for new quantum circuit
            thisSpin = 0
            config = quantumRNG(4)
        #end the game
        if choice == 6:
            print("Thanks for playing!")
            print("\n\nAaron Lewis")
            playAgain = False

#prints the current quantum circuit
def viewCircuit(qCirc):
    print(qCirc.draw())
    return

#prompts for bet info and returns info to main
def bet(currentFunds, hiBet, loBet, evenBet, oddBet, numberBet, numberSel):
    if currentFunds < 1:
        print("Insufficient credits. Please add more credits to play")
        return
    betChoice = 0
    betChoice = int(input("1. Bet Low (1-15)\n2. Bet High (16-30)\n3. Bet Even\n4. Bet Odd\n5. Bet on a Number\n"))
    while betChoice < 1 and betChoice > 6:
        print("Please select an option from the menu!")
        betChoice = int(input("1. Bet Low (1-15)\n2. Bet High (16-30)\n3. Bet Even\n4. Bet Odd\n5. Bet on a Number\n"))

    if betChoice == 1:
        if loBet > 0:
            print("You've already bet on low!\n")
        else:
            loBet = int(input("Enter your bet: "))
            if loBet > currentFunds:
                loBet = 0
                print("You can only bet up to " + str(currentFunds) + "\n")
            currentFunds -= loBet
    elif betChoice == 2:
        if hiBet > 0:
            print("You've already bet on high!\n")
        else:
            hiBet = int(input("Enter your bet: "))
            if hiBet > currentFunds:
                hiBet = 0
                print("You can only bet up to " + str(currentFunds) + "\n")
            currentFunds -= hiBet
    elif betChoice == 3:
        if evenBet > 0:
            print("You've already bet on even!\n")
        else:
            evenBet = int(input("Enter your bet: "))
            if evenBet > currentFunds:
                evenBet = 0
                print("You can only bet up to " + str(currentFunds) + "\n")
            currentFunds -= evenBet
    elif betChoice == 4:
        if oddBet > 0:
            print("You've already bet on odd!\n")
        else:
            oddBet = int(input("Enter your bet: "))
            if oddBet > currentFunds:
                oddBet = 0
                print("You can only bet up to " + str(currentFunds) + "\n")
        currentFunds -= oddBet
    elif betChoice == 5:
        if numberBet > 0:
            print("You've already bet on a number!\n")
        else:
            numberSel = 0
            while numberSel < 1 or numberSel > 30:
                numberSel = int(input("Please select a number between 1 and 30: " ))
            numberBet = int(input("Enter your bet: "))
            if numberBet > currentFunds:
                numberBet = 0
                print("You can only bet up to " + str(currentFunds) + "\n")
        currentFunds -= numberBet
    return currentFunds, hiBet, loBet, evenBet, oddBet, numberBet, numberSel

#lets you add arbitrary amount of credits to play
def addCredits(funds):
    added = 0
    added += int(input("Enter the number of credits you would like to purchase: "))
    while added < 0:
        print("Please enter a positive value!")
        added += int(input("Enter the number of credits you would like to purchase: "))
    funds += added
    return funds

#displays the menu options and returns the selection
def menu():
    choice = int(input("1. Make Bet\n2. Spin\n3. Add Credits\n4. Rules\n5. View Circuit\n6. Quit\n"))
    while choice < 1 or choice > 6:
        print("Please select an option from the menu!\n")
        choice = int(input("1. Make Bet\n2. Spin\n3. Add Credits\n4. Rules\n5. View Circuit\n6. Quit\n"))
    return choice

def printRules():
    print("*********************************************************************************")
    print("*\n* Game Rules")
    print("*\n* Qubits are measured after passing through the gates on the quantum circuit")
    print("* Their measurements are stored in the classical registers and they represent a")
    print("* binary number between 0 and 31")
    print("*\n* Bet High - if you 'spin' the quantum roulette wheel and the binary value")
    print("*        is greater than 15 and not equal to 31 then you win double your bet\n*")
    print("* Bet Low - if you 'spin' the quantum roulette wheel and the binary value")
    print("*        is less than 16 and not equal to 0 then you win double your bet\n*")
    print("* Bet Even - if you 'spin' the quantum roulette wheel and the binary value")
    print("*        is even and not equal to 0 then you win double your bet\n*")
    print("* Bet Odd - if you 'spin' the quantum roulette wheel and the binary value")
    print("*        is odd and not equal to 31 then you win double your bet\n*")
    print("* Bet Number - if you 'spin' the quantum roulette wheel and the binary value")
    print("*        is equal to the number you bet on, not 0 or 31, you win 30x your bet\n*")
    print("*********************************************************************************")
    
#calculates the winnings based on the rules of the game and returns the winnings
def calcWinnings(thisSpin, betHigh, betLow, betEven, betOdd, betNumber, numberSelected):
    winnings = 0    # init winnings to 0
    if betHigh > 0 and thisSpin > 15 and thisSpin < 31: #pay double bet for bet high winnings
        winnings += 2*betHigh
        betHigh = 0
    if betLow > 0 and thisSpin < 16 and thisSpin > 0:   #pay double bet for bet low winnings
        winnings += 2*betLow
        betLow = 0
    if betEven > 0 and thisSpin%2 == 0 and thisSpin != 0: #pay double bet for bet even winnings
        winnings += 2*betEven
        betEven = 0
    if betOdd > 0 and thisSpin%2 == 1 and thisSpin != 31: #pay double bet for be odd winnings
        winnings += 2*betOdd
        betOdd = 0
    if betNumber > 0 and thisSpin == numberSelected: #pay 30x bet for bet number winnings
        print(betNumber)
        print(numberSelected)
        winnings += 30*betNumber
        betNumber = 0
        numberSelected = 0
    return winnings

#calls main to play the game
main()
 
