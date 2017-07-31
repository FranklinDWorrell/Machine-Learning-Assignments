# Code used to provide preliminary linear regression analysis of data 
# concerning forest fires in Portugal. Five different models are considered
# for both the full data set and the top five features as determined
# by magnitude of PCC. Regularization is subsequently applied to the
# models making use of polynomial curves of magnitude 6 or higher.
# 
# @author   Franklin D. Worrell
# @version  23 February 2016
# 
# Programming Assignment #1
# Machine Learning 1
# CSCI 6990-603
# Spring 2017 

#################################################
################ Preliminaries ##################
#################################################

# Read in raw data. 
Raw = dlmread('cleandata.csv',',');

# Calculate and store Pearson CCs. 
PCC = [1:12];
for i = 1:12
  PCC(1,i) = corr(Raw(:,i),Raw(:,13));
endfor

# Add initial x0 value of 1. 
Data = ones(517,14); 
for i = 1:13
  Data(:,i+1) = Raw(:,i);
endfor 


#################################################
############# Function Definitions ##############
#################################################

# Pulls the previously determined top five features from 
# a set of data.
# @param  CVSet the set of data to pull features from 
# @return A matrix that is the top five features of the data 
function retval = topFive (CVSet)
  retval = CVSet(:,[1 10 11 7 2 4 14]);
endfunction # end function topFive 

# Calculates the Beta values for a given X and Y matrix. 
# @param  X matrix of independent variables' values 
# @param  Y matrix of dependent variable values
# @return   a matrix of the Beta values for each x in X
function retval = calculateBetaValues (X, Y)
  retval = pinv(X'*X)*X'*Y; 
endfunction # end function calculateBetaValues 

# Calculates the Beta values for a given X and Y matrix 
# while applying a lambda value for regularization. 
# @param  lambda  the lambda value 
# @param  X matrix of independent variables' values 
# @param  Y matrix of dependent variable values
# @return   a matrix of the Beta values for each x in X
function retval = calculateRegBeta(lambda, X, Y)
  MLambda = eye(size(X,2)); 
  MLambda(1,1) = 0; 
  retval = pinv(X'*X + lambda*MLambda)*X'*Y; 
endfunction # end function calculateRegBeta

# Given a model, produces the avg and SD of both the MAE and 
# RMSE of that model. Can apply regularization with a given
# lambda value if those fields are provided. Applies 10-fold 
# cross validation. 
# @param  Model       the model being evaluated 
# @param  regularize  0 if model should NOT be regularized
# @param  lambda      lambda value if model should be regularized 
# @return a vector containing avg MAE, SD of MAE, avg RMSE, and SD of RMSE for Model 
function retval = processModel (Model, regularize, lambda)
  rows = size(Model,1);     # Saving dimensions of Model matrix in variables
  columns = size(Model,2);  #   makes subsequent code more readable. 
  k = 10;             # Number of folds in kFCV. 
  MAEs = ones(1,k);   # Vector to hold MAE of each fold. 
  RMSEs = ones(1,k);  # Vector to hold RMSE of each fold. 
  
  # For each fold k = 1..10. 
  for i = 1:k 
    # Divide the data into training and testing sets. 
    Test = ones(1,columns); 
    Train = ones(1,columns); 
    testIndex = 1; 
    trainIndex = 1; 
    for j = 1:rows 
      if (mod(j,10) == i - 1) 
        Test(testIndex,:) = Model(j,:); 
        testIndex++;
      else 
        Train(trainIndex,:) = Model(j,:); 
        trainIndex++; 
      endif 
    endfor # end division of data into training and testing sets 
    
    # Calculate Beta vector for the model on this fold. 
    Beta = ones(1, (columns - 1));    # Matrix to hold Beta values 
    # Model does not require regularization. 
    if (regularize == 0) 
      Beta = calculateBetaValues(Train(:,1:(columns - 1)), Train(:,columns)); 
    # Model should be regularized. 
    else 
      Beta = calculateRegBeta(lambda, Train(:,1:(columns - 1)), Train(:,columns)); 
    endif 
    
    # Calculate this fold's MAE and RMSE. 
    n = size(Test,1);   
    sumAbs = 0; 
    sumSQ = 0; 
    # Find error for each input in test data. 
    for q = 1:n 
      error = Test(q,columns) - (Test(q,1:(columns - 1))*Beta);
      sumAbs += abs(error); 
      sumSQ += error^2; 
    endfor 
    MAEs(1,i) = sumAbs / n; 
    RMSEs(1,i) = sqrt(sumSQ / n); 
  endfor # end determining statistics for each of k folds. 

  # Calculate average of MAE across all k folds. 
  avgMAE = sum(MAEs) / k; 
  # Calculate SD of MAE across all k folds. 
  SDMAE = std(MAEs);

  # Calculate average of RMSE across all k folds. 
  avgRMSE = sum(RMSEs) / k;
  # Calculate SD of RMSE across all k folds. 
  SDRMSE = std(RMSEs); 
  
  retval = [avgMAE, SDMAE, avgRMSE, SDRMSE]; 
endfunction # end function processModel

# Regularizes higher-order models using the non-iterative
# equation. Lambda values are predetermined as specified in
# the assignment. 
# @param  Model the model to evaluate with regularization 
# @return   a matrix of regularized Beta values for each x in X
function retval = regularize (Model)
  LambdaVals = [0.5E-8, 1.5E-6, 2.0E-4, 1, 2];  # Lambda values. 
  Errors = ones(5, 5);  # Matrix to hold error for each lambda value. 
  
  # Calculate the error for each of the provided lambda. 
  for i=1:5
    Errors(i,:) = [LambdaVals(1,i), processModel(Model, 1, LambdaVals(1,i))]; 
  endfor 
  
  retval = Errors; 
endfunction # end function regularize 


#################################################
#################### Part A #####################
#################################################

# Model 1 is raw input features. 
Table2 = ones(5, 5); 
Table3 = ones(5, 5);
# Process Model 1 for both sets of features. 
Table2(1,:) = [1, processModel(Data, 0, 0)];
Table3(1,:) = [1, processModel(topFive(Data), 0, 0)]; 

# Model 2: 
#   In 12 feature model: 
#     Square feature 2, 
#     Square feature 7, 
#     Multiply feature 12 by feature 5.
M2 = [Data(:,1:13), ones(size(Data,1), 3), Data(:,14)]; 
M2(:,14) = M2(:,3).^2; 
M2(:,15) = M2(:,8).^2;
M2(:,16) = M2(:,13).*M2(:,6);     # POSSIBLE PROBLEM
#   In 5 feature model: 
#     Square feature 9 (the top feature), 
#     Square feature 10 (the second feature), 
#     ln(feature 6) (the third feature).
M1T5 = topFive(Data); 
M2T5 = [M1T5(:,1:6), ones(size(M1T5,1), 3), M1T5(:,7)];
M2T5(:,7) = M2T5(:,2).^2;
M2T5(:,8) = M2T5(:,3).^2; 
M2T5(:,9) = log(M2T5(:,4));
# Process Model 2 for both sets of features.
Table2(2,:) = [2, processModel(M2, 0, 0)];
Table3(2,:) = [2, processModel(M2T5, 0, 0)]; 

# Model 4:
#   In 12 feature model:
#     Feature 11 ^ 4,
#     Feature 5 ^ 3,
#     ln(Feature 5). 
M4 = [M2(:,1:16), ones(size(M2,1), 3), M2(:,17)]; 
M4(:,17) = M4(:,12).^4; 
M4(:,18) = M4(:,6).^3; 
M4(:,19) = log(M4(:,5)); 
#   In 5 feature model: 
#     Cube feature 3 (the fifth feature),
#     Feature 1 (the fourth feature) ^ 4, 
#     Feature 9 * Feature 6. 
M4T5 = [M2T5(:,1:9), ones(size(M2T5,1), 3), M2T5(:,10)]; 
M4T5(:,10) = M4T5(:,6).^3; 
M4T5(:,11) = M4T5(:,5).^4; 
M4T4(:,12) = M4T5(:,2).*M4T5(:,4); 
# Process Model 4 for both sets of features.
Table2(3,:) = [4, processModel(M4, 0, 0)];
Table3(3,:) = [4, processModel(M4T5, 0, 0)]; 

# Model 6: 
#   In 12 feature model:
#     Feature 3 ^ 6,
#     Feature 9 ^ 5. 
M6 = [M4(:,1:19), ones(size(M4,1), 2), M4(:,20)]; 
M6(:,20) = M6(:,4).^6;
M6(:,21) = M6(:,10).^5;
#   In 5 feature model: 
#     Feature 6 (the third feature) ^ 6, 
#     (Feature 9 (the first feature) ^ 2) / Feature 10.
M6T5 = [M4T5(:,1:12), ones(size(M4T5,1), 2), M4T5(:,13)]; 
M6T5(:,13) = M6T5(:,4).^6; 
M6T5(:,14) = M6T5(:,7)./M6T5(:,3);
# Process Model 6 for both sets of features.
Table2(4,:) = [6, processModel(M6, 0, 0)];
Table3(4,:) = [6, processModel(M6T5, 0, 0)]; 

# Model 9:
#   In 12 feature model: 
#     Feature 9 * Feature 7,
#     Feature 11 ^ 9.
M9 = [M6(:,1:21), ones(size(M6,1), 2), M6(:,22)]; 
M9(:,22) = M9(:,10).*M9(:,8); 
M9(:,23) = M9(:,12).^9; 
#   In 5 feature model:
#     (Feature 9 * Feature 10) ^ 9, 
#     Feature 6 ^ 9. 
M9T5 = [M6T5(:,1:14), ones(size(M6T5,1), 2), M6T5(:,15)]; 
M9T5(:,15) = (M9T5(:,2).*M9T5(:,3)).^9; 
M9T5(:,16) = M9T5(:,4).^9; 
# Process Model 9 for both sets of features. 
Table2(5,:) = [9, processModel(M9, 0, 0)];
Table3(5,:) = [9, processModel(M9T5, 0, 0)]; 

# Write expanded data sets for both sets of features
csvwrite('12featureM9.csv', M9); 
csvwrite('top5featureM9.csv', M9T5); 

# Write Part A results to csv files. 
csvwrite('table2.csv', Table2); 
csvwrite('table3.csv', Table3); 

# Plot Graph#6: Model vs. MAE (12 feature)
plot(Table2(:,1), Table2(:,2), 'Marker', 'o')
title('Graph 6: Model vs. MAE (12 Features)')
xlabel('Model')
ylabel('MAE')
print('Graph_6', '-dpdf')

# Plot Graph#7: Model vs. RMSE (12 feature)
plot(Table2(:,1), Table2(:,4), 'Marker', 'o')
title('Graph 7: Model v. RMSE (12 Features)')
xlabel('Model')
ylabel('RMSE')
print('Graph_7', '-dpdf')

# Plot Graph#8: Model vs. MAE (Top 5)
plot(Table3(:,1), Table3(:,2), 'Marker', 'o')
title('Graph 8: Model vs. MAE (Top 5 Features)')
xlabel('Model')
ylabel('MAE')
print('Graph_8', '-dpdf')

# Plot Graph#9: Model vs. RMSE (Top 5)
plot(Table3(:,1), Table3(:,4), 'Marker', 'o')
title('Graph 9: Model v. RMSE (Top 5 Features)')
xlabel('Model')
ylabel('RMSE')
print('Graph_9', '-dpdf')



#################################################
#################### Part B #####################
#################################################

Table4 = ones(6, 5);  # Table for M6 regularization data.
Table4(1,:) = [0, Table2(4,2:5)]; 
Table4(2:6,:) = regularize(M6); 

Table5 = ones(6, 5);  # Table for M9 regularization data. 
Table5(1,:) = [0, Table2(5,2:5)];
Table5(2:6,:) = regularize(M9);  

Table6 = ones(6, 5);  # Table for M6T5 regularization data. 
Table6(1,:) = [0, Table3(4,2:5)];
Table6(2:6,:) = regularize(M6T5);  

Table7 = ones(6, 5);  # Table for M9T5 regularization data. 
Table7(1,:) = [0, Table3(5,2:5)]; 
Table7(2:6,:) = regularize(M9T5); 

# Write Part B results to csv files. 
csvwrite('table4.csv', Table4); 
csvwrite('table5.csv', Table5); 
csvwrite('table6.csv', Table6); 
csvwrite('table7.csv', Table7); 

# Construct graphs for Models 6 and 9 utilizing all 12 Features. 

# Plot Graph#10: ln(Lambda) vs. ln(MAE) Model 6 (12 Features)
plot(log(Table4(:,1)), log(Table4(:,2)), 'Marker', 'o')
title('Graph 10: Lambda vs. MAE for Model 6 (12 Features)')
xlabel('ln(Lambda)')
ylabel('ln(MAE)')
print('Graph_10', '-dpdf')

# Plot Graph#11: ln(Lambda) vs. ln(RMSE) Model 6 (12 Features)
plot(log(Table4(:,1)), log(Table4(:,4)), 'Marker', 'o')
title('Graph 11: Lambda vs. MAE for Model 6 (12 Features)')
xlabel('ln(Lambda)')
ylabel('ln(RMSE)')
print('Graph_11', '-dpdf')

# Plot Graph#12: ln(Lambda) vs. ln(MAE) Model 9 (12 Features)
plot(log(Table5(:,1)), log(Table5(:,2)), 'Marker', 'o')
title('Graph 12: Lambda vs. MAE for Model 9 (12 Features)')
xlabel('ln(Lambda)')
ylabel('ln(MAE)')
print('Graph_12', '-dpdf')

# Plot Graph#13: ln(Lambda) vs. ln(RMSE) Model 9 (12 Features)
plot(log(Table5(:,1)), log(Table5(:,4)), 'Marker', 'o')
title('Graph 13: Lambda vs. MAE for Model 9 (12 Features)')
xlabel('ln(Lambda)')
ylabel('ln(RMSE)')
print('Graph_13', '-dpdf')

# Construct graphs for Models 6 and 9 utilizing all 12 Features. 

# Plot Graph#14: ln(Lambda) vs. ln(MAE) Model 6 (Top 5 Features)
plot(log(Table6(:,1)), log(Table6(:,2)), 'Marker', 'o')
title('Graph 14: Lambda vs. MAE for Model 6 (Top 5 Features)')
xlabel('ln(Lambda)')
ylabel('ln(MAE)')
print('Graph_14', '-dpdf')

# Plot Graph#15: ln(Lambda) vs. ln(RMSE) Model 6 (Top 5 Features)
plot(log(Table6(:,1)), log(Table6(:,4)), 'Marker', 'o')
title('Graph 15: Lambda vs. MAE for Model 6 (Top 5 Features)')
xlabel('ln(Lambda)')
ylabel('ln(RMSE)')
print('Graph_15', '-dpdf')

# Plot Graph#16: ln(Lambda) vs. ln(MAE) Model 9 (Top 5 Features)
plot(log(Table7(:,1)), log(Table7(:,2)), 'Marker', 'o')
title('Graph 16: Lambda vs. MAE for Model 9 (Top 5 Features)')
xlabel('ln(Lambda)')
ylabel('ln(MAE)')
print('Graph_16', '-dpdf')

# Plot Graph#17: ln(Lambda) vs. ln(RMSE) Model 9 (Top 5 Features)
plot(log(Table7(:,1)), log(Table7(:,4)), 'Marker', 'o')
title('Graph 17: Lambda vs. MAE for Model 9 (Top 5 Features)')
xlabel('ln(Lambda)')
ylabel('ln(RMSE)')
print('Graph_17', '-dpdf')