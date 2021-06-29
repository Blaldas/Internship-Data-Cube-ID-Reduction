package notUsingFastUtil;


import reducedIDStorageMiexCrompressionChangedSubCubeQuery.DIntArray;

import java.util.Arrays;

public class DataCube {

    ShellFragment[] shellFragmentList;
    int lower;


    /**
     * @param sizes      Max value of each dimension
     * @param lowerValue o menor valor do dataset (default = 1)
     *                   Chamada uma vez para criar o objeto cubo
     */
    public DataCube(int[] sizes, int lowerValue) {
        shellFragmentList = new ShellFragment[sizes.length];
        this.lower = lowerValue;
        for (int i = 0; i < shellFragmentList.length; i++) {
            shellFragmentList[i] = new ShellFragment(lowerValue, sizes[i]);
        }
    }

    /**
     * @param tid    this tuple id
     * @param values Array of values to each dimension.
     */
    public void addTuple(int tid, int[] values) {


        for (int i = 0; i < shellFragmentList.length; i++)
            shellFragmentList[i].addTuple(tid, values[i]);
    }


    /**
     * prints all the values and the number of tuples they store, for every single diemension
     */
    public void showAllDimensions() {
        for (int i = 0; i < shellFragmentList.length; i++) {
            StringBuilder str = new StringBuilder();
            System.out.println("Dimension " + (i + 1));
            System.out.println("Value\tNumberTuples");
            for (int n = shellFragmentList[i].lower; n <= shellFragmentList[i].upper; n++) {
                str.append(n).append("\t").append(Arrays.toString(shellFragmentList[i].getTidsListFromValue(n)));
                str.append("\n");
            }
            System.out.println(str);
            System.out.println(shellFragmentList[i].matrix[0].length);
        }
    }

    /**
     * @param query the query being made
     * @return array with the tuple ids, null if query.length != shellFragmentList.length
     */
    public int[] pointQuerySeach(int[] query) {
        if (query.length != shellFragmentList.length)
            return null;

        int instanciated = 0;
        int[][] tidsList = new int[shellFragmentList.length][];                 //stores values of instanciated
        for (int i = 0; i < query.length; i++) {                                        //obtem todas as listas de values
            if (query[i] != -88 && query[i] != -99) {
                int[] returned = shellFragmentList[i].getTidsListFromValue(query[i]);
                if (returned.length == 0)                                      //se a lista for vazia, devolve lista com tamanho 0
                    return new int[0];
                else if (instanciated == 0)
                    tidsList[0] = returned;      //obtem lista de tids
                else {

                    for (int n = instanciated - 1; n >= 0; n--) {
                        if (tidsList[n].length > returned.length) {
                            tidsList[n + 1] = tidsList[n];
                            if (n == 0)
                                tidsList[0] = returned;
                        } else {
                            tidsList[n + 1] = returned;
                            break;
                        }
                    }
                }
                //tidsList[instanciated] = returned;
                instanciated++;
            }
        }

        int[] returnable = tidsList[0];
        if (instanciated > 0) {
            for (int i = 1; i < instanciated; i++) {
                returnable = intersect(returnable, tidsList[i]);
                if (returnable.length == 0)
                    return returnable;
                tidsList[i] = null; //chama para o garabge colector
            }

            return returnable;
        }

        return shellFragmentList[0].getAllTids();
    }


    public int[] pointQuerySeachSubCube(ShellFragment[] subCube, int[] query) {
        if (query.length != subCube.length)
            return null;

        int instanciated = 0;
        int[][] tidsList = new int[subCube.length][];                 //stores values of instanciated
        for (int i = 0; i < query.length; i++) {                                        //obtem todas as listas de values
            if (query[i] != -88) {
                int[] returned = subCube[i].getTidsListFromValueWithoutPronage(query[i]);
                if (returned.length == 0)                                      //se a lista for vazia, devolve lista com tamanho 0
                    return new int[0];
                else if (instanciated == 0)
                    tidsList[0] = returned;      //obtem lista de tids
                else {

                    for (int n = instanciated - 1; n >= 0; n--) {
                        if (tidsList[n].length > returned.length) {
                            tidsList[n + 1] = tidsList[n];
                            if (n == 0)
                                tidsList[0] = returned;
                        } else {
                            tidsList[n + 1] = returned;
                            break;
                        }
                    }
                }
                //tidsList[instanciated] = returned;
                instanciated++;
            }
        }

        int[] d = tidsList[0];
        if (instanciated > 0) {
            for (int i = 1; i < instanciated; i++) {

                d = intersect(d, tidsList[i]);

                if (d.length == 0)
                    return d;
            }

            return d;
        }
        return subCube[0].getAllTids();
    }


    /**
     * @param arrayA
     * @param arrayB
     * @return chamar conuntos com o menor numero de tuples possivel
     */
    private static int[] intersect(int[] arrayA, int[] arrayB) {
        int[] c = new int[Math.min(arrayA.length, arrayB.length)];
        int ai = 0, bi = 0, ci = 0;

        while (ai < arrayA.length && bi < arrayB.length) {
            if (arrayA[ai] == arrayB[bi]) {
                if (ci == 0 || arrayA[ai] != c[ci - 1]) {
                    //if (arrayA[ai] != 0) {  Esta verificação foi removida porque os arrays enviados para aqui têm o tamanho estritamente necessário
                    //porém, esta linha estava a ignorar o tid 0 e a gastar tempo precioso
                    //Caso possa receber arrays com tamanho maior que o necessário, a linha pode ser usada como:
                    //if (arrayA[ai] != 0 && ai != 0)
                    c[ci++] = arrayA[ai];
                    //}
                }
                ai++;
                bi++;
            } else if (arrayA[ai] > arrayB[bi]) {
                bi++;
            } else if (arrayA[ai] < arrayB[bi]) {
                ai++;
            }
        }

        return Arrays.copyOfRange(c, 0, ci);
    }


    public int getNumberShellFragments() {
        return shellFragmentList.length;
    }

    public int getNumberTuples() {
        return shellFragmentList[0].getBiggestTid() + 1;
    }

    /**
     * @param values the query
     */
    public void getSubCube(int[] values) {
        if (values.length != shellFragmentList.length) {
            System.out.println("wrong number of dimensions");
            return;
        }

        int[] tidArray = this.pointQuerySeach(values);            //obtem TIDs resultante
        if (tidArray == null || tidArray.length == 0) {
            System.out.println("no values found");
            return;
        }

        //mostra resposta a query inicial:
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == -99 || values[i] == -88)
                str.append('*').append(" ");
            else
                str.append(values[i]).append(" ");
        }
        str.append(": ").append(tidArray.length);
        System.out.println(str);


        System.out.println("A recriar sub dataset");
        //para cada tid resultante
        int numInqiridas = 0;
        for (int i : values)
            if (i == -99)
                numInqiridas++;

        int[] mapeamentoDimInq = new int[numInqiridas];
        numInqiridas = 0;
        for (int i = 0; i < values.length; i++)
            if (values[i] == -99)
                mapeamentoDimInq[numInqiridas++] = i;


        int subdataset[][] = new int[tidArray.length][numInqiridas];//cada linha é uma tupla, cada coluna é uma dimensão;

        //para cada dimensão, obtem os tids, interceta com os tids do subCubo e adiciona os valores:
        for (int d = 0; d < mapeamentoDimInq.length; d++) {     //para cada uma das dimensões inquiridas
            for (int i = 0; i < shellFragmentList[mapeamentoDimInq[d]].matrix.length; i++) {      //para cada valor da dimensão
                int[] val = shellFragmentList[mapeamentoDimInq[d]].matrix[i];                     //obtem lista de tids com esse valor
                //note-se: val tem tamanho exato.
                //faz interceção: val com tidArray
                int ti = 0;
                int vi = 0;
                while (ti < tidArray.length && vi < val.length) {   //interceção e adiciona
                    if (tidArray[ti] == val[vi]) {          //se igual
                        subdataset[ti][d] = i + lower;    //lower igual a 1 para todas as diemnsões!
                        ++ti;
                        ++vi;
                    } else if (tidArray[ti] < val[vi])
                        ++ti;
                    else
                        ++vi;
                }
            }
        }

       // System.out.println(Arrays.deepToString(subdataset));
       // System.exit(0);
        System.out.println("A refazer cubo");
        ShellFragment[] subCube = new ShellFragment[numInqiridas];
        for (int i = 0; i < subCube.length; i++) {
            subCube[i] = new ShellFragment(shellFragmentList[mapeamentoDimInq[i]].lower, shellFragmentList[mapeamentoDimInq[i]].upper);
        }

        for (int i = 0; i < subdataset.length; i++)
            for (int d = 0; d < subCube.length; d++)
                subCube[d].addTuple(i, subdataset[i][d]);

        System.out.println("Subcubo acabado");

        //System.out.println(Arrays.deepToString(subdataset));
        showQueryDataCube(values, mapeamentoDimInq, subCube);

    }

    /**
     * @param qValues the query
     * @param subCube the resulted subCube
     */
   /* private void showQueryDataCube(int[] qValues, ShellFragment[] subCube) {

        int[] query = new int[subCube.length];               //stores all the values as a query.
        int[] counter = new int[subCube.length];             //counter to the query values
        for (int c : counter)
            c = 0;

        int[][] values = getAllDifferentValues(qValues);      //guarda todos os valores diferentes para cada dimensão


        int total = 1;                              //guarda o numero de conbinações difrerentes
        for (int[] d : values) {
            total *= (d.length);
        }

        int[][] arrayQueriesEResultados = new int[total][qValues.length + 1];

        int rounds = 0;
        do {
            for (int i = 0; i < counter.length; i++)     //da os valores as queries
                arrayQueriesEResultados[rounds][i] = query[i] = values[i][counter[i]];


            //pesquisa e mostra com valores do query
            arrayQueriesEResultados[rounds][qValues.length] = pointQueryCounterSubCube(subCube, query, qValues);// faz pesquisa sobre esses valores

            //gere os counters
            for (int i = 0; i < counter.length; i++) {              //para cada um dos counter
                if (counter[i] < values[i].length - 1) {
                    counter[i]++;
                    break;
                } else        //if( counter[i] == '*')
                    counter[i] = 0;

            }

            rounds++;
        } while (rounds < total);

 /*       StringBuilder str = new StringBuilder();
        for (int[] q : arrayQueriesEResultados) {
            str.setLength(0);
            for (int i = 0; i < q.length - 1; i++) {
                if (q[i] == -88)
                    str.append('*').append(" ");
                else if (q[i] == -99)
                    str.append('?').append(" ");
                else
                    str.append(q[i]).append(" ");
            }
            str.append(" : ").append(q[q.length - 1]);
            System.out.println(str);
        }




        System.out.println(total + " lines written");
    }

    */
    private void showQueryDataCube(int[] qValues, int[][] subCube) {

        int[] query = new int[qValues.length];               //stores all the values as a query.
        int[] counter = new int[qValues.length];             //counter to the query values
        for (int c : counter)
            c = 0;

        int[][] values = getAllDifferentValues(qValues);      //guarda todos os valores diferentes para cada dimensão


        int total = 1;                              //guarda o numero de conbinações difrerentes
        for (int[] d : values) {
            total *= (d.length);
        }

        int[][] arrayQueriesEResultados = new int[total][qValues.length + 1];

        int c = 0;
        for (int i : qValues)
            if (i == -99)
                c++;
        int[] dimensoesInquiridas = new int[c];
        c = 0;
        for (int i = 0; i < qValues.length; i++)
            if (qValues[i] == -99)
                dimensoesInquiridas[c++] = i;

        //dimensoesInquiridas contem os indices das diemnsões inquiridas

        System.out.println(Arrays.toString(dimensoesInquiridas));
        int rounds = 0;
        boolean flag = true;
        do {
            for (int i = 0; i < counter.length; i++)     //da os valores as queries
                arrayQueriesEResultados[rounds][i] = query[i] = values[i][counter[i]];
            arrayQueriesEResultados[rounds][qValues.length] = 0;


            for (int[] tuple : subCube) {
                flag = true;
                for (int i = 0; i < dimensoesInquiridas.length; i++) {
                    if (tuple[dimensoesInquiridas[i]] != query[dimensoesInquiridas[i]] && query[dimensoesInquiridas[i]] != -88) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    ++arrayQueriesEResultados[rounds][qValues.length];
            }

            //gere os counters
            for (int i = 0; i < counter.length; i++) {              //para cada um dos counter
                if (counter[i] < values[i].length - 1) {
                    counter[i]++;
                    break;
                } else        //if( counter[i] == '*')
                    counter[i] = 0;

            }

            rounds++;
        } while (rounds < total);

        StringBuilder str = new StringBuilder();
        for (int[] q : arrayQueriesEResultados) {
            str.setLength(0);
            for (int i = 0; i < q.length - 1; i++) {
                if (q[i] == -88)
                    str.append('*').append(" ");
                else if (q[i] == -99)
                    str.append('?').append(" ");
                else
                    str.append(q[i]).append(" ");
            }
            str.append(" : ").append(q[q.length - 1]);
            System.out.println(str);
        }




        System.out.println(total + " lines written");
    }

    private void showQueryDataCube(int[] qValues, int[] mapeamentoDimInq, ShellFragment[] subCube) {

        int[] query = new int[subCube.length];               //stores all the values as a query.
        int[] counter = new int[subCube.length];             //counter to the query values
        for (int c : counter)
            c = 0;

        int[][] values = getAllDifferentValues(qValues);      //guarda todos os valores diferentes para cada dimensão


        int total = 1;                              //guarda o numero de conbinações difrerentes
        for (int[] d : values) {
            total *= (d.length);
        }
        
        System.gc();
        int[][] arrayQueriesEResultados = new int[total][qValues.length + 1];

        int rounds = 0;
        do {
            for (int i = 0; i < counter.length; i++)     //da os valores as queries
                query[i] = values[mapeamentoDimInq[i]][counter[i]];

            for (int i = 0; i < qValues.length; i++)
                arrayQueriesEResultados[rounds][i] = qValues[i];

            for (int i = 0; i < mapeamentoDimInq.length; i++)
                arrayQueriesEResultados[rounds][mapeamentoDimInq[i]] = query[i];

            //pesquisa e mostra com valores do query
            arrayQueriesEResultados[rounds][qValues.length] = pointQueryCounterSubCube(subCube, query);// faz pesquisa sobre esses valores

            //System.out.println(Arrays.toString(query) + " : " + arrayQueriesEResultados[rounds][qValues.length]);


            //gere os counters
            for (int i = 0; i < mapeamentoDimInq.length; i++) {              //para cada um dos counter
                if (counter[i] < values[mapeamentoDimInq[i]].length - 1) {
                    counter[i]++;
                    break;
                } else        //if( counter[i] == '*')
                    counter[i] = 0;
            }
            ++rounds;
        } while (rounds < total);

/*        StringBuilder str = new StringBuilder();
        for (int[] q : arrayQueriesEResultados) {
            str.setLength(0);
            for (int i = 0; i < q.length - 1; i++) {
                if (q[i] == -88)
                    str.append('*').append(" ");
                else if (q[i] == -99)
                    str.append('?').append(" ");
                else
                    str.append(q[i]).append(" ");
            }
            str.append(" : ").append(q[q.length - 1]);
            System.out.println(str);
        }
 */



        System.out.println(total + " lines written");
    }

    /**
     * @param queryValues the query
     * @return a matrix with all the values to be looped, in each dimension.
     */
    private int[][] getAllDifferentValues(int[] queryValues) {
        int[][] result = new int[queryValues.length][1];

        for (int i = 0; i < queryValues.length; i++) {               //para cada uma das dimensões
            if (queryValues[i] == -99) {
                result[i] = new int[shellFragmentList[i].matrix.length + 1];
                result[i][0] = -88;
                for (int j = result[i].length; j > 1; result[i][--j] = j) {
                }
            } else {
                result[i][0] = queryValues[i];
            }
        }
        return result;
    }


    public int pointQueryCounterSubCube(ShellFragment[] subCube, int[] query) {
        int[] mat = pointQuerySeachSubCube(subCube, query);
        if (mat == null)
            return -1;
        return mat.length;
    }


    /**
     * @param tid tuple id to seach on
     * @return an array with the dimensional values of such tuple.
     */
    private int[] getDimensions(int tid, int[] query) {
        int[] returnable = new int[shellFragmentList.length];

        for (int i = 0; i < query.length; i++) {//query.length == shellfragmentelist.length
            if (query[i] == -99 || query[i] == -88)      //se estiver instanciado tem de ter estes valores
                returnable[i] = shellFragmentList[i].getValueFromTid(tid);
            else {                                       //se não tiver instanciado tem de se procurar
                returnable[i] = query[i];
            }
        }

        return returnable;
    }

    public int getNumberUnnusedInts() {
        int total = 0;
        for (ShellFragment d : shellFragmentList)
            total += d.getNumberUnnusedInts();
        return total;
    }

    public int getNumberUsedInts() {
        int total = 0;
        for (ShellFragment d : shellFragmentList)
            total += d.getNumberUsedInts();
        return total;
    }

    public int getBiggestID() {
        return shellFragmentList[0].getBiggestTid();
    }

    public void proneShellfragments() {
        for (ShellFragment s : shellFragmentList)
            s.proneShellFragment();
    }
}