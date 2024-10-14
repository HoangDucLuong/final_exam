package shop.repository;

import java.util.List;
import shop.model.ContractDetail;

public interface ContractDetailRepository {
    void saveContractDetail(ContractDetail contractDetail);
    List<ContractDetail> getContractDetailsByContractId(int contractId);
    void deleteContractDetailsByContractId(int contractId);
}
