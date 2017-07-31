# MATLAB/Octave script used to generate required
# results for Programming Assignment 3. The 
# output is a three column matrix correspoding 
# to the table required in the report for the 
# assignment. Each entry is a percent error of a
# predictor, and the final row is average error 
# of the column above it. Ten-fold cross 
# validation was used, and column collects the 
# percent error of a different predictor. From 
# left to right, the predictors are Linear
# Discriminant Analysis, Quadratic Discriminant 
# Analysis, and Logistic Regression. Requires 
# input file named data.csv to be located in the 
# current working directory at runtime. 
# 
# @author   Franklin D. Worrell
# @version  4 May 2017
# 
# Programming Assignment #3
# Machine Learning 1
# CSCI 6990-603
# Spring 2017 

#################################################
################ Preliminaries ##################
#################################################

# Read in slightly modified data file. Only change
# from provided arff file is removal of header info. 
Data = dlmread('data.csv',','); 

# To properly format data, "class" column needs to
# be altered so that values are 0 or 1 instead 
# of 1 or 2--this facilitates using the Logistic 
# Regression predictor. 
for i = 1:size(Data, 1)
  Data(i,size(Data, 2)) = Data(i,size(Data, 2)) - 1; 
endfor
  

#################################################
################## Functions ####################
#################################################

# Calculates the Mu vector for a data set for the 
# outcome class specified as a parameter. 
# 
# @param  DataSet   the data 
# @param  class     the class number for analysis 
# @return a Mu vector for the output class 
function retval = getMuForClass(DataSet, class) 
  Mu = zeros(1, (size(DataSet, 2) - 1)); 
  numberRelevantObservations = 0; 
  
  # Sum the values of each column for given outcome. 
  for i = 1:size(DataSet, 1) 
    if (DataSet(i, size(DataSet, 2)) == class) 
      for j = 1:(size(DataSet, 2) - 1)
        Mu(1,j) = Mu(1,j) + DataSet(i, j); 
        numberRelevantObservations++; 
      endfor 
    endif 
  endfor 
  
  
  # Divide each element by number of observations. 
  Mu = Mu./numberRelevantObservations; 
  
  retval = Mu'; 
endfunction     # end function getMuForClass


# Performs the basis expansion for training and 
# running the quadratic discriminant analysis 
# algorithm on a set of data. 
# 
# @param  DataSet   the data set for basis expansion 
function retval = expandBasisForQDA(DataSet)
  expandedBasis = [DataSet(:,1:(size(DataSet, 2) - 1)), ones(size(DataSet, 1), 4), DataSet(:,size(DataSet, 2))]; 
  expandedBasis(:,size(DataSet, 2)) = expandedBasis(:,2).^2; 
  expandedBasis(:,(size(DataSet, 2) + 1)) = expandedBasis(:,3).*expandedBasis(:,7); 
  expandedBasis(:,(size(DataSet, 2) + 2)) = sqrt(expandedBasis(:,6)); 
  expandedBasis(:,(size(DataSet, 2) + 3)) = log(expandedBasis(:,4)); 
  retval = expandedBasis; 
endfunction     # end function expandBasisForQDA


# Adds an initial column of 1s to observations for
# aid in calculating y-intercept value. Needed for 
# Logistic Regression algorithm implementation. 
#
# @param  DataSet the observations being modified
# @return the DataSet expanded with an initial vector of 1s
function retval = addColumnOfOnes(DataSet) 
  newData = [ones(size(DataSet, 1), 1), DataSet(:,:)]; 
  retval = newData; 
endfunction     # end function addColumnOfOnes



# Runs linear discriminant analysis algorithm on 
# a training set, tests on a test set, and then 
# returns the percent error of the predictor. 
# 
# @param  TrainSet  the training set for this fold
# @param  TestSet   the testing set for this fold 
# @return the error rate for this fold using LDA  
function retval = runLDAAndGetErrorRate(TrainSet, TestSet)
  Mu0 = getMuForClass(TrainSet, 0); 
  Mu1 = getMuForClass(TrainSet, 1); 
  Sigma = cov(TrainSet(:,1:(size(TrainSet, 2) - 1))); 
  
  # Test model and calculate and return error rate. 
  numberIncorrectlyClassified = 0.0; 
  for i = 1:size(TestSet, 1) 
    # Get prediction. 
    Delta0 = (TestSet(i,1:(size(TestSet, 2) - 1)) * pinv(Sigma) * Mu0 - 
              0.5 * Mu0' * pinv(Sigma) * Mu0 + log(0.5));
    Delta1 = (TestSet(i,1:(size(TestSet, 2) - 1)) * pinv(Sigma) * Mu1 - 
              0.5 * Mu1' * pinv(Sigma) * Mu1 + log(0.5));
    # Check for accuracy and record result. 
    if (((Delta0 >= Delta1) && (TestSet(i,size(TestSet, 2)) == 1)) || 
        ((Delta0 < Delta1) && (TestSet(i,size(TestSet, 2)) == 0)))
      numberIncorrectlyClassified = numberIncorrectlyClassified + 1.0; 
    endif 
  endfor 
  
  retval = (numberIncorrectlyClassified / size(TestSet, 1));
endfunction     # end function runLDAAndGetErrorRate


# Runs quadratic discriminant analysis algorithm 
# on a training set, tests on a test set, and then 
# returns the percent error of the predictor. 
# 
# @param  TrainSet  the training set for this fold
# @param  TestSet   the testing set for this fold 
# @return the error rate for this fold using QDA  
function retval = runQDAAndGetErrorRate(TrainSet, TestSet)
  # Expand the data set. 
  TrainSet = expandBasisForQDA(TrainSet); 
  TestSet = expandBasisForQDA(TestSet); 
  
  ### Collect all components for training QDA. ###
  # Mu vectors for both outcomes. 
  Mu0 = getMuForClass(TrainSet, 0); 
  Mu1 = getMuForClass(TrainSet, 1); 
  # Divide training data based on outcome. 
  TrainClass0 = ones(1, size(TrainSet, 2)); 
  class0RowNumber = 1; 
  TrainClass1 = ones(1, size(TrainSet, 2)); 
  class1RowNumber = 1; 
  for i = 1:size(TrainSet, 1)
    if (TrainSet(i, size(TrainSet, 2)) == 0) 
      TrainClass0(class0RowNumber,:) = TrainSet(i,:); 
      class0RowNumber++; 
    else 
      TrainClass1(class1RowNumber,:) = TrainSet(i,:); 
      class1RowNumber++; 
    endif 
  endfor 
  # Covariance matrices calculated by Octave function. 
  Sigma0 = cov(TrainClass0(:,1:(size(TrainClass0, 2) - 1))); 
  Sigma1 = cov(TrainClass1(:,1:(size(TrainClass1, 2) - 1))); 
  
  # Test model and calculate and return error rate. 
  numberIncorrectlyClassified = 0.0; 
  for i = 1:size(TestSet, 1) 
    # Get prediction. 
    Delta0 = (-0.5 * log(det(Sigma0)) - 0.5 * 
        (TestSet(i,1:(size(TestSet, 2) - 1))' - Mu0)' * 
        pinv(Sigma0) * (TestSet(i,1:(size(TestSet, 2) - 1))' - Mu0) + log(0.5));
    Delta1 = (-0.5 * log(det(Sigma1)) - 0.5 * 
        (TestSet(i,1:(size(TestSet, 2) - 1))' - Mu1)' * 
        pinv(Sigma1) * (TestSet(i,1:(size(TestSet, 2) - 1))' - Mu1) + log(0.5));    
    # Check for accuracy and record result. 
    if (((Delta0 >= Delta1) && (TestSet(i,size(TestSet, 2)) == 1)) || 
        ((Delta0 < Delta1) && (TestSet(i,size(TestSet, 2)) == 0)))
      numberIncorrectlyClassified = numberIncorrectlyClassified + 1.0; 
    endif 
  endfor 
  
  retval = (numberIncorrectlyClassified / size(TestSet, 1));
endfunction     # end function runQDAAndGetErrorRate


# Runs logistic regression algorithm on a training
# set, tests on a test set, and then returns the 
# percent error of the predictor. 
# 
# @param  TrainSet  the training set for this fold
# @param  TestSet   the testing set for this fold 
# @return the error rate for this fold using LR 
function retval = runLRAndGetErrorRate(TrainSet, TestSet) 
  # Add initial x0 value of 1.
  TrainSet = addColumnOfOnes(TrainSet); 
  TestSet = addColumnOfOnes(TestSet); 
  
  # Separate X and Y values for TrainSet. 
  X = TrainSet(:,1:(size(TrainSet, 2) - 1)); 
  Y = TrainSet(:, size(TrainSet, 2)); 
  
  # Implementation of Logistic Regression algorithm. 
  Beta = zeros(size(X, 2), 1); 
  W = eye(size(X, 1), size(X, 1)); 
  Eta = zeros(size(X, 1), 1); 
  
  # Build Beta vector to sufficient precision. 
  for j = 1:10
    for i = 1:size(X, 1)
      x = X(i,:)'; 
      p = exp(x' * Beta) / (1 + exp(x' * Beta)); 
      Eta(i) = p; 
      p = p * (1 - p); 
      W(i,i) = p; 
    endfor
    
    z = (X * Beta) + (pinv(W) * (Y - Eta)); 
    Beta = pinv(X' * W * X) * X' * W * z; 
  endfor 
  
  # Test model and calculate and return error rate. 
  numberIncorrectlyClassified = 0.0; 
  for i = 1:size(TestSet, 1) 
    # Get prediction. 
    yCap = TestSet(i,1:(size(TestSet, 2) - 1)) * Beta; 
    # Check for accuracy and record result. 
    if (((yCap >= 0.5) && (TestSet(i,size(TestSet, 2)) == 0)) || 
        ((yCap < 0.5) && (TestSet(i,size(TestSet, 2)) == 1)))
      numberIncorrectlyClassified = numberIncorrectlyClassified + 1.0; 
    endif 
  endfor 
  
  retval = (numberIncorrectlyClassified / size(TestSet, 1));
endfunction     # end function runLRAndGetErrorRate


#################################################
############### Main Procedure ##################
################################################# 

# Perform 10FCV on data, calculate error rate for
# each fold for LDA, QDA, and LR. Store result in
# table to report error rates for all folds and 
# average across all folds. 

Results = ones(11, 3);    # Error rates to report 

# For each fold in 10FCV. 
for i = 1:10 
  # Divide the data into training and testing sets. 
  Test = ones(1, size(Data, 2)); 
  Train = ones(1, size(Data, 2)); 
  testIndex = 1; 
  trainIndex = 1; 
  for j = 1:size(Data, 1) 
    if (mod(j,10) == (i - 1)) 
      Test(testIndex,:) = Data(j,:); 
      testIndex++;
    else 
      Train(trainIndex,:) = Data(j,:); 
      trainIndex++; 
    endif 
  endfor # End dividing training and testing sets 
  
  Results(i, 1) = runLDAAndGetErrorRate(Train, Test);
  Results(i, 2) = runQDAAndGetErrorRate(Train, Test); 
  Results(i, 3) = runLRAndGetErrorRate(Train, Test);
endfor   # End loop through each fold in 10FCV 

# Calculate the average error rate accross all ten 
# folds for each classifier and add the result to the
# matrix of results. 
for m = 1:size(Results, 2) 
  sum = 0.0;
  for n = 1:(size(Results, 1) - 1)
    sum = sum + Results(n, m); 
  endfor
  Results(size(Results, 1), m) = sum / (size(Results, 1) - 1);  
endfor 

# Ouput table of results to terminal and/or to file. 
#dlmwrite('averageErrors.csv', Results, ','); 
Results 

