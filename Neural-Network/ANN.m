# Creates five ANNs each with a number of hidden layers as specified in the 
# matrix below. 
#
# An expansion of code provided by Prof. T. Hoque for the project. 
# 
# Franklin D. Worrell
# 19 April 2018
# 
# Programming Assignment 2 
# Machine Learning 2 
# CSCI 6990-001 
# Spring 2018

# Constants through run of script. 
alpha = 0.2;        # Usually alpha < 0, ranging from 0.1 to 1
target_mse = 0.05;  # One of the exit conditions
Max_Epoch = 1000;   # One of the exit conditions
K = 3;              # Number of classes--hard-coded 
folds = 10;         # Number of cross-validation folds 

# Load Iris Dataset. It must be contained in this directory as iris.data.txt. 
RawData = dlmread('iris.data.txt', ','); 
rawDimensions = size(RawData); 
P = rawDimensions(1, 2) - 1;         # Number of features 

################################################################################
############## Build the structure of each of the five ANNs ####################
################################################################################
HiddenLayersPerANN=[1 3 5 6 7];   # Number of layers in each of five ANNs. 
Layers=cell(length(HiddenLayersPerANN), 1); 
# Define each layer of all of five ANNs. 
for i = 1:length(HiddenLayersPerANN) 
  Layers{i} = ones(1, HiddenLayersPerANN(i) + 2); 
  # The number of input and output nodes is the same in each ANN. 
  Layers{i}(1) = P; 
  Layers{i}(HiddenLayersPerANN(i) + 2) = K; 
  for  j = 2:length(Layers{i}) - 1 
    # Each hidden layer has 2 to 20 nodes. 
    Layers{i}(j) = round(rand() * 18) + 2; 
  end 
end 
# Write the ANN structures to a file. 
csvwrite('ANN_structure.csv', cell2mat(Layers')'); 

# The matrix used to record the minimum test error of each fold for each ANN. 
ErrorsForReporting = ones(length(HiddenLayersPerANN), folds + 1); 

################################################################################
###################### Process Each of the Five ANNs ###########################
################################################################################
for i = 1:3    #length(HiddenLayersPerANN) 
  L = Layers{i} 
  # Used to track the best model for this ANN across all folds. 
  Min_ANN_Error = Inf; 
  # Used to record average MSE training error rate for first 200 epochs. 
  Ave_Epoch_Train_MSE = zeros(1, 200); 
  # Used to record average classification training error rate for first 200 epochs. 
  Ave_Epoch_Class_Train_Err = zeros(1, 200); 
  # Used to record average MSE test error rate for first 200 epochs. 
  Ave_Epoch_Test_MSE = zeros(1, 200); 
  # Used to record average classification test error rate for first 200 epochs. 
  Ave_Epoch_Class_Test_Err = zeros(1, 200); 

  ##############################################################################
  ############## For each ANN, apply ten-fold cross validation #################
  ##############################################################################
  for f = 1:folds 
    Min_Error = Inf;
    epoch = 0;    # One forward sweep of the net for each training sample 
    mse = Inf;    # Initializing MSE with a very large value.
    Err = [];
    Epo = []; 
    Min_Test_Error = Inf; 
    TestErr = []; 
    
    # Forming the number of Beta/weight matrix needed in between the layers
    B = cell(length(L) - 1, 1);  
    # Accumulator for changes to make to Beta--these will be averaged. 
    BSums = cell(length(L) - 1, 1); 
    for a = 1:length(L) - 1 
      # Assign uniform random values in [-0.7, 0.7] 
      B{a} = [1.4 .* rand(L(a) + 1, L(a + 1)) - 0.7];	
      # Assign zeroes to accumulator. 
      BSums{a} = zeros(L(a) + 1, L(a + 1)); 
    end 

    # Allocate places for Term, T 
    T = cell(length(L), 1);
    for e = 1:length(L)
      T{e} = ones(L(e), 1);
    end

    # Allocate places for activation, i.e., Z
    Z = cell(length(L), 1);
    for c = 1:length(L) - 1
      Z{c} = zeros(L(c) + 1, 1);  # It does not matter how we initialize (with '0' or '1', or whatever,) this is fine!
    end
    Z{end} = zeros(L(end), 1);    # At the final layer there is no Bias unit 
    
    # Allocate places for error term delta, d
    d = cell(length(L), 1);
    for g = 1:length(L)
      d{g} = zeros(L(g), 1);
    end

    ############################################################################
    ################ Perform the data partition for this fold ##################
    ############################################################################
    X = ones(1, P);     # Training data 
    Y = ones(1, 1); 
    TestX = ones(1, P); # Test data 
    TestY = ones(1, 1); 
    trainIndex = 1; 
    testIndex = 1; 
    for j = 1:size(RawData, 1) 
      # Since data sorted by outcome, modulus is used to partition. 
      if (mod(j, folds) == (f - 1)) 
        TestX(testIndex,:) = RawData(j, 1:P); 
        TestY(testIndex,:) = RawData(j, P + 1); 
        testIndex = testIndex + 1;
      else 
        X(trainIndex, :) = RawData(j, 1:P);
        Y(trainIndex, :) = RawData(j, P + 1);  
        trainIndex = trainIndex + 1; 
      end 
    end 
    
    # Variables regarding data this fold. 
    Nx = size(X)(1); 
    Ny = size(Y)(1); 
    NTx = size(TestX)(1); 
    NTy = size(TestY)(1); 

    ############################################################################
    ########################## Training Section ################################
    ############################################################################
    while (mse > target_mse) && (epoch < Max_Epoch) 
      
      CSqErr = 0; 		  # Cumulative Sq Err of each Sample. 
      numMisclass = 0;  # Number of classification errors this fold. 
    
      for k = 1:Nx    # Process each training datum 		
        Z{1} = [X(k,:) 1]';   # Load Inputs with bias = 1
        Yk = [0 0 0]';        # Load outcomes with 1 for the correct 
        Yk(Y(k,:)', 1) = 1;   #   class and 0 for the other two. 
      
        ########################################################################
        ######################## Forward Propagation ###########################
        ########################################################################
        for m = 1:length(L) - 1
          T{m + 1} = B{m}' * Z{m}; 
                
          if (m + 1) < length(L)
            Z{m + 1} = [(1 ./ (1 + exp(-T{m + 1}))) ; 1]; 
          else  
            Z{m + 1} = (1 ./ (1 + exp(-T{m + 1}))); 
          end 
        end  # End of forward propagation 
             
        # Collect sample wise errors. 
        CSqErr = CSqErr + sum((Yk - Z{end}) .^ 2); 
        [prob predClass] = max(Z{end}'); 
        if predClass ~= Y(k,1) 
          numMisclass = numMisclass + 1; 
        endif 

        ########################################################################
        ### Compute error term for each of the nodes except the input unit. ####
        ########################################################################
        # Error term for the output layer. 
        d{end} = (Z{end}-Yk) .* Z{end} .* (1-Z{end}); 
        
        # Compute the error term for all the hidden layers (skip the input layer).
        for o = length(L) - 1:-1:2 
          d{o} = Z{o}(1:end-1) .* (1-Z{o}(1:end-1)) .* sum(B{o}(1:end-1,:) * d{o+1}); 
        end 
        
        # Compute change in Beta to be averaged later. 
        for u = 1:length(L) - 1 
          weightUpdate = alpha .* (Z{u}(1:end - 1) * d{u + 1}');
          biasUpdate = alpha .* d{u + 1}';
          BSums{u}(1:end - 1, :) = BSums{u}(1: end - 1, :) + weightUpdate; 
          BSums{u}(end, :) = BSums{u}(end, :) + biasUpdate; 
        end 
      end  # End processing each training datum 
      
      # Update MSE and best values found so far. 
      mse = (CSqErr) / (3 * Nx);   # Average error of N sample after an epoch 
      # Update the classification error rate. 
      CER = numMisclass / Nx; 
      epoch  = epoch + 1;

      # Record statistics for plotting. 
      if epoch < 201
        Ave_Epoch_Train_MSE(epoch) = Ave_Epoch_Train_MSE(epoch) + mse; 
        Ave_Epoch_Class_Train_Err(epoch) = Ave_Epoch_Class_Train_Err(epoch) + CER; 
      endif 
        
      Err = [Err mse];
      Epo = [Epo epoch];   

      if mse < Min_Error
        Min_Error = mse;
      end 
            
      ##########################################################################
      ####################### Batch Back-Propagation ###########################
      ##########################################################################
      # Average the new Beta terms and update the weights. 
      for p = 1:length(L) - 1
        B{p} = B{p} - (BSums{p} ./ Nx); 
      end 
      
      ##########################################################################
      ############################ Test Section ################################
      ##########################################################################
      CSqTestErr = 0; 
      numMisclassTest = 0; 
      CERT = 0; 
      
      for r = 1:NTx    # Test loop 
        Z{1} = [TestX(r, :) 1]';    # Load Inputs with bias = 1
        Yk = [0 0 0]';              # Load outcomes with 1 for the correct 
        Yk(TestY(r, :)' , 1) = 1;   #   class and 0 for the other two. 
        
        # Forward propagation of test data 
        for s = 1:length(L) - 1
          T{s+1} = B{s}' * Z{s}; 
                
          if (s + 1) < length(L)
            Z{s + 1} = [(1 ./ (1 + exp(-T{s + 1}))) ; 1]; 
          else  
            Z{s + 1} = (1 ./ (1 + exp(-T{s + 1}))); 
          end 
        end  # End of forward propagation of test data 
             
        # Collect sample wise cumulative square error. 
        CSqTestErr = CSqTestErr + sum((Yk - Z{end}) .^ 2); 
        [prob predClassTest] = max(Z{end}'); 

        # Update cumulative square error statistics. 
        if CSqTestErr < Min_Test_Error 
          Min_Test_Error = CSqTestErr; 
        end 
        
        # Update classification error statistics. 
        if predClassTest ~= TestY(r,1) 
          numMisclassTest = numMisclassTest + 1; 
        endif 
      end # Test loop 
      
      # Update MSE stats. 
      TestMSE = CSqTestErr / (3* NTx); 
      TestErr = [TestErr TestMSE]; 
	    mse = TestMSE; 	# Added at Hoque's suggestion. 
      # Update classification error stats. 
      CERT = numMisclassTest / NTx; 

      if epoch < 201
        Ave_Epoch_Class_Test_Err(epoch) = Ave_Epoch_Class_Test_Err(epoch) + CERT; 
        Ave_Epoch_Test_MSE(epoch) = Ave_Epoch_Test_MSE(epoch) + TestMSE; 
      endif
    end   # End while loop 
    
    # Store the lowest error found in this fold. 
    ErrorsForReporting(i, f) = Min_Test_Error;     
  end   # End of cross validation loop 
  
  # Compute average MSE error rates per epoch. 
  Ave_Epoch_Train_MSE = Ave_Epoch_Train_MSE ./ folds; 
  Ave_Epoch_Test_MSE = Ave_Epoch_Test_MSE ./ folds; 
  
  # Compute average classification error rates per epoch. 
  Ave_Epoch_Class_Train_Err = Ave_Epoch_Class_Train_Err ./ folds; 
  Ave_Epoch_Class_Test_Err = Ave_Epoch_Class_Test_Err ./ folds; 
  
  # Plot the average MSE for the first 200 epochs for this ANN. 
  fig = figure('visible', 'off') 
  plot (Epo(1:200), Ave_Epoch_Train_MSE(1:200), Epo(1:200), Ave_Epoch_Test_MSE(1:200)) 
  title(['ANN ' num2str(i) ': Training/Test MSE by Epoch'])
  legend('Train', 'Test') 
  xlabel('Epoch')
  ylabel('MSE')
  print([num2str(i) '_MSE'], '-dpdf')

  # Plot the average classification error for the first 200 epochs for this ANN. 
  plot (Epo(1:200), Ave_Epoch_Class_Train_Err(1:200), Epo(1:200), Ave_Epoch_Class_Test_Err(1:200)) 
  title(['ANN ' num2str(i) ': Training/Test Classification Error by Epoch'])
  legend('Train', 'Test') 
  xlabel('Epoch')
  ylabel('Classification Error')
  print([num2str(i) '_Classification'], '-dpdf')
end    # End processing each ANN 

################################################################################
##### Compute average of minumum test error across all folds for each ANN ######
################################################################################
for i = 1:length(HiddenLayersPerANN) 
  ErrorsForReporting(i, folds + 1) = sum(ErrorsForReporting(i,1:folds)) / folds; 
end 
# Write the minimum test errors to a file for easy reporting. 
csvwrite('minimum_test_errors.csv', ErrorsForReporting); 
