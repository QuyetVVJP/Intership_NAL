import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Device } from '../device/device';
import { UserDto } from '../user/user';
import { Deviceloan, DeviceLoanDto } from './devicve-loan';

@Injectable({
  providedIn: 'root'
})
export class DeviceLoanService {
  private baseURL ="http://localhost:8080/loan"
  constructor(private httpClient: HttpClient) { }
  getAllLoan(): Observable<DeviceLoanDto[]> {
    return this.httpClient.get<DeviceLoanDto[]>(`${this.baseURL + '/list'}`);
  }
  createLoan(loan:  Deviceloan): Observable<Object> {
    return this.httpClient.post<Deviceloan[]>(`${this.baseURL + '/create'}`, loan);
  }
  getLoanById(id: number): Observable<Deviceloan> {
    return this.httpClient.get<Deviceloan>(`${this.baseURL}/${id}`);
  }

  getAllDeviceWithPagination(term): Observable<any> {
    let terms = new HttpParams().set('term', term);
    return this.httpClient.get<Device[]>(`${this.baseURL + '/search'}`,{ params: terms } );
  }
  getUserLogin():Observable<UserDto>{
    return this.httpClient.get<UserDto>(`${this.baseURL + '/userIsLogin'}`);
  }
  approveLoan(id:number): Observable<Object> {
    return this.httpClient.post<Deviceloan[]>(`${this.baseURL + '/approve'}`, id);
  }
  unapproveLoan(id:number): Observable<Object> {
    return this.httpClient.post<Deviceloan[]>(`${this.baseURL + '/unapprove'}`, id);
  }
}
